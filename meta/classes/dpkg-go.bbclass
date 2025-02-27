# This software is a part of ISAR.
# Copyright (C) 2025 Siemens AG
#
# SPDX-License-Identifier: MIT

inherit dpkg

# override this to "1" if you need to download go modules before entering the sbuild chroot
GOPRECACHE ?= "0"
# override this to use private registries (comma-separated), e.g. foo.bar.com/*
GOPRIVATE ?= ""
# override this to provide go build tags
GOTAGS ?= ""

SBUILD_PASSTHROUGH_ADDITIONS += "GO111MODULE GOPROXY GOARCH GOARM"

# ccache support is unstable when pre-fetching
USE_CCACHE = "0"

DEBIAN_BUILD_DEPENDS:append = ", \
    golang${@':native' if d.getVar('ISAR_CROSS_COMPILE') == '1' else ''}, \
    git, \
    "

GOARCH ?= ""
GOARM ?= ""

python() {
    if d.getVar('GOPRECACHE', True) == '1':
        d.appendVarFlag('do_prepare_build', 'depends', ' host-go:do_deploy_hostgo')
        d.appendVarFlag('do_prepare_build', 'cleandirs', ' ${S}/vendor')

    # user hasn't set GOARCH, try to set it automagically
    if not d.getVar('GOARCH', True):
        # map DEB_ARCH to GOARCH
        mapping = {
            'amd64': 'amd64',
            'arm64': 'arm64',
            'riscv64': 'riscv64',
        }
        distro_arch = d.getVar('DISTRO_ARCH', True)
        goarch = mapping.get(distro_arch)
        if goarch:
            d.setVar('GOARCH', goarch)
        else:
            # no direct mapping found, try a little harder
            if goarch == 'armel':
                d.setVar('GOARCH', 'arm')
                d.setVar('GOARM', '5')
            elif goarch == 'armhf':
                d.setVar('GOARCH', 'arm')
                d.setVar('GOARM', '6')
}

# copied and adapted from fetch2.__init__
get_fetch_env[vardepsexclude] += "BB_ORIGENV"
def get_fetch_env(d):
    exportvars = ['GIT_PROXY_COMMAND',
                  'GIT_SSH',
                  'SSH_AUTH_SOCK', 'SSH_AGENT_PID']
    cmd = ''
    origenv = d.getVar("BB_ORIGENV", False)
    for var in exportvars:
        val = d.getVar(var) or (origenv and origenv.getVar(var))
        if val:
            cmd = 'export ' + var + '=\"%s\"; %s' % (val, cmd)
    return cmd

def get_git_config(goprivate):
    import re
    hosts = set()
    for priv in goprivate.split(','):
        m = re.search(r'^\s*(?:\w+\://)?([^/]*)(?:/.*)?$', priv)
        if m:
            hosts.add(m.group(1))
    return '; '.join(['git config --global url."ssh://git@%s/".insteadOf "https://%s/"' % (h, h) for h in hosts])

do_prepare_build[network] = "${TASK_USE_NETWORK}"
do_prepare_build[cleandirs] += "${S}/debian ${S}/.gocache ${S}/.gomodcache"
do_prepare_build[vardepsexclude] += "GO111MODULE GOPROXY GIT_PROXY_COMMAND GIT_SSH SSH_AUTH_SOCK SSH_AGENT_PID"
do_prepare_build[dirs] += "${WORKDIR}/go"
do_prepare_build() {
    deb_debianize

    if [ "${GOPRECACHE}" = "1" ]; then
        ${@get_fetch_env(d)}
        E="${@isar_export_proxies(d)}"
        export HOME="${WORKDIR}"
        export GO111MODULE="${GO111MODULE}"
        export GOPROXY="${GOPROXY}"
        export GOCACHE="${S}/.gocache"
        export GOMODCACHE="${S}/.gomodcache"
        export GOPRIVATE="${GOPRIVATE}"
        # enforce clone via ssh
        ${@get_git_config(d.getVar('GOPRIVATE'))}
        (cd ${S} && ${DEPLOY_DIR}/host-go/bin/go mod vendor)
    fi
}

deb_create_rules:append() {
    # if downstream provides no 'rules' file, create a sane default, assuming 'go build' just works
    cat << EOF >> ${S}/debian/rules
export GOCACHE := \$(CURDIR)/.gocache
export GOMODCACHE := \$(CURDIR)/.gomodcache

override_dh_auto_build:
	go build \
		-trimpath \
		-ldflags="-s -w" \
		-mod=${@'vendor' if d.getVar('GOPRECACHE') == '1' else 'readonly'} \
		-tags "${GOTAGS}" \
		-o "${PN}"

override_dh_strip:
	# it's a bad idea to 'strip' go binaries with non-go tooling, see
	# https://github.com/moby/moby/blob/2a95488f7843a773de2b541a47d9b971a635bfff/project/PACKAGERS.md#stripping-binaries
	true

override_dh_auto_install:
	install -D -m0755 "${PN}" "debian/${PN}/usr/bin/${PN}"
EOF
}
