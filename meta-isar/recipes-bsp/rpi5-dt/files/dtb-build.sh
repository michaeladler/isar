#!/bin/sh
set -e

SOURCE1=disable-vc4-overlay.dts
SOURCE2=uboot-bcm2835-pl011-overlay.dts
SOURCE3=disable-v3d-overlay.dts
SOURCE4=enable-bt-overlay.dts
SOURCE5=smbios-overlay.dts
SOURCE6=fixup-blconfig-overlay.dts
 

PPDIR=out
mkdir -p $PPDIR

export DTC_FLAGS="-R 4 -p 0x1000 -@ -H epapr"
for dts in arch/arm/boot/dts/broadcom/bcm27*dts arch/arm64/boot/dts/broadcom/bcm27*dts; do
    target=$(basename ${dts%*.dts})
    cpp -x assembler-with-cpp -undef -D__DTS__ -DFIRMWARE_UPDATED -nostdinc -I. -Iinclude/ -Iscripts/dtc/include-prefixes/ -P $dts -o $PPDIR/$target.dts
    dtc $DTC_FLAGS -I dts -O dtb -i ./$(dirname $dts) -o $PPDIR/$target.dtb $PPDIR/$target.dts
done

export DTC_FLAGS="-R 0 -p 0 -@ -H epapr"
for dts in arch/arm/boot/dts/overlays/*dts ${SOURCE1} ${SOURCE2} ${SOURCE3} ${SOURCE4} ${SOURCE5} ${SOURCE6}; do
    target=$(basename ${dts%*.dts})
    target=${target%*-overlay}
    mkdir -p $PPDIR/overlays
    cpp -x assembler-with-cpp -undef -D__DTS__ -DFIRMWARE_UPDATED -nostdinc -I. -Iinclude/ -Iscripts/dtc/include-prefixes/ -P $dts -o $PPDIR/overlays/$target.dts
    dtc $DTC_FLAGS -I dts -O dtb -i ./$(dirname $dts) -o $PPDIR/overlays/$target.dtbo $PPDIR/overlays/$target.dts
done

# These are loaded implicitly by the RPi 5 firmware and it
# expect these exact names.
mv $PPDIR/overlays/hat_map.dtbo $PPDIR/overlays/hat_map.dtb
mv $PPDIR/overlays/overlay_map.dtbo $PPDIR/overlays/overlay_map.dtb

# Include README file
cp arch/arm/boot/dts/overlays/README $PPDIR/overlays/
