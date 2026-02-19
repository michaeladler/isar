# This software is a part of ISAR.
# Copyright (C) 2026 Siemens

inherit dpkg

DESCRIPTION = "Raspberry Pi 5 firmware blobs"
MAINTAINER = "isar-users <isar-users@googlegroups.com>"

_REV = "16be713c42900303109a1962d9eade1defef0986"

# adopted from https://build.opensuse.org/package/show/openSUSE:Factory/raspberrypi-firmware-dt
SRC_URI = " \
    https://github.com/raspberrypi/linux/archive/${_REV}.tar.gz;unpack=0 \
    file://rules \
    file://Makefile \
    file://dtb-build.sh \
    file://disable-v3d-overlay.dts \
    file://disable-vc4-overlay.dts \
    file://enable-bt-overlay.dts \
    file://fixup-blconfig-overlay.dts \
    file://smbios-overlay.dts \
    file://uboot-bcm2835-pl011-overlay.dts \
    file://0001-ARM-dts-bcm2711-rpi-Reuse-bcm2836-vchiq-driver.patch;apply=0 \
    file://0001-ARM-dts-bcm27xx-Use-better-name-for-spidev.patch;apply=0 \
    file://0001-Revert-bcm2711-rpi-ds-Switch-to-dma40-channel-for-hd.patch;apply=0 \
    file://0002-ARM-dts-bcm2711-Fix-xHCI-power-domain.patch;apply=0 \
    file://0001-dts-rp1-Wrap-RP1-node-into-nexus-node-as-expected-by.patch;apply=0 \
    file://0001-ARM-dts-bcm2712-Remove-DMA-support.patch;apply=0 \
    file://0001-ARM-dts-bcm2712-Slow-down-eMMC-interface.patch;apply=0 \
    file://bcm2712-fix-compatible.patch;apply=0 \
    file://0001-Amend-the-RP1-ethernet-node-to-work-with-upstream-dr.patch;apply=0 \
    file://0001-dts-overlays-Adjust-them-for-RPi5.patch;apply=0 \
    file://0001-dts-bcm2712-Extend-PCIe-range-to-encompass-firmware-.patch;apply=0 \
"
SRC_URI[sha256sum] = "1b7de4fdfa08abee37ce1700ff4f5de72afdf6df9fa2b685182dd76a7c685a71"

DEBIAN_BUILD_DEPENDS = "device-tree-compiler"

DPKG_ARCH = "all"

do_prepare_build[cleandirs] += "${S}/debian"
do_prepare_build() {
    deb_debianize
    cp -v ${WORKDIR}/*.dts ${WORKDIR}/dtb-build.sh ${WORKDIR}/Makefile ${S}/

    cd ${S}/
    tar --strip-components=1 -xzvf ${WORKDIR}/${_REV}.tar.gz linux-${_REV}/arch linux-${_REV}/include linux-${_REV}/scripts

    patch -p1 <${WORKDIR}/0001-ARM-dts-bcm2711-rpi-Reuse-bcm2836-vchiq-driver.patch
    patch -p1 <${WORKDIR}/0001-ARM-dts-bcm27xx-Use-better-name-for-spidev.patch
    patch -p1 <${WORKDIR}/0001-Revert-bcm2711-rpi-ds-Switch-to-dma40-channel-for-hd.patch
    patch -p1 <${WORKDIR}/0002-ARM-dts-bcm2711-Fix-xHCI-power-domain.patch
    patch -p1 <${WORKDIR}/0001-dts-rp1-Wrap-RP1-node-into-nexus-node-as-expected-by.patch
    patch -p1 <${WORKDIR}/0001-ARM-dts-bcm2712-Remove-DMA-support.patch
    patch -p1 <${WORKDIR}/0001-ARM-dts-bcm2712-Slow-down-eMMC-interface.patch
    patch -p1 <${WORKDIR}/bcm2712-fix-compatible.patch
    patch -p1 <${WORKDIR}/0001-Amend-the-RP1-ethernet-node-to-work-with-upstream-dr.patch
    patch -p1 <${WORKDIR}/0001-dts-overlays-Adjust-them-for-RPi5.patch
    patch -p1 <${WORKDIR}/0001-dts-bcm2712-Extend-PCIe-range-to-encompass-firmware-.patch
}
