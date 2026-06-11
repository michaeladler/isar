# This software is a part of Isar.
# Copyright (C) 2026 Siemens

inherit dpkg

DESCRIPTION = "Raspberry Pi firmware blobs"
MAINTAINER = "isar-users <isar-users@googlegroups.com>"

SRC_URI = " \
    https://github.com/raspberrypi/firmware/archive/${PV}.tar.gz;downloadfilename=${PN}-${PV}.tar.gz \
    file://debian/install \
    file://debian/rules \
"
SRC_URI[sha256sum] = "b900de58571920a306ab2d1296500499d0744451dbefda0c76b79e652bb71cb7"

S = "${WORKDIR}/firmware-${PV}"

do_prepare_build[cleandirs] += "${S}/debian"
do_prepare_build() {
    deb_debianize
    cp -r ${WORKDIR}/debian ${S}
}
