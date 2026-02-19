#
# This software is a part of ISAR.
# Copyright (c) Siemens AG, 2026
#
# SPDX-License-Identifier: MIT

inherit u-boot

SRC_URI += " \
    https://ftp.denx.de/pub/u-boot/u-boot-${PV}.tar.bz2 \
    file://rpi5_extra.config \
"
SRC_URI[sha256sum] = "b60d5865cefdbc75da8da4156c56c458e00de75a49b80c1a2e58a96e30ad0d54"

DEBIAN_BUILD_DEPENDS .= ", libssl-dev:${DISTRO_ARCH}, libssl-dev:native \
    , libgnutls28-dev:native, libgnutls28-dev:${DISTRO_ARCH}, \
"

S = "${WORKDIR}/u-boot-${PV}"

COMPATIBLE_MACHINE = "^rpi\d?-arm64.*"

U_BOOT_CONFIG = "rpi_arm64_defconfig"

U_BOOT_BIN_INSTALL = "u-boot.bin"

do_prepare_build:append() {
    cat ${WORKDIR}/rpi5_extra.config >> ${S}/configs/rpi_arm64_defconfig
}
