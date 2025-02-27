# This software is a part of ISAR.
# Copyright (C) 2025 Siemens AG
#
# SPDX-License-Identifier: MIT

SRC_URI = "https://go.dev/dl/go${PV}.linux-${HOST_ARCH}.tar.gz"
SHA256SUM_amd64 = "dea9ca38a0b852a74e81c26134671af7c0fbe65d81b0dc1c5bfe22cf7d4c8858"
SHA256SUM_arm64 = "c3fa6d16ffa261091a5617145553c71d21435ce547e44cc6dfb7470865527cc7"
SRC_URI[sha256sum] = "${SHA256SUM_${HOST_ARCH}}"

do_deploy_hostgo[dirs] += "${DEPLOY_DIR}"
do_deploy_hostgo() {
    cp -rl ${WORKDIR}/go ${DEPLOY_DIR}/host-go
}
addtask deploy_hostgo after do_unpack before do_build

CLEANFUNCS = "clean_deploy"
clean_deploy() {
    rm -rf ${DEPLOY_DIR}/host-go
}
