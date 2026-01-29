# This software is a part of ISAR.
# Copyright (C) 2026 Siemens

inherit dpkg

DESCRIPTION = "Raspberry Pi 5 firmware blobs"
MAINTAINER = "isar-users <isar-users@googlegroups.com>"

# patched dtb's, see https://build.opensuse.org/package/show/openSUSE%3AFactory/raspberrypi-firmware-dt
SRC_URI = " \
    https://download.opensuse.org/tumbleweed/repo/oss/noarch/raspberrypi-firmware-dt-2025.05.14-6.1.noarch.rpm;downloadfilename=${PN}-${PV}.rpm;unpack=0 \
    file://rules \
"
SRC_URI[sha256sum] = "ff16a33d2ca1f5b93b931864ad4890076745d101ecfa81f459826fde16252203"

DEBIAN_BUILD_DEPENDS = "rpm2cpio, cpio"

S = "${WORKDIR}/${PN}-${PV}"

DPKG_ARCH = "all"

do_prepare_build[cleandirs] += "${S}/debian"
do_prepare_build() {
    deb_debianize
    cp ${WORKDIR}/${PN}-${PV}.rpm ${S}/${PN}.rpm
}
