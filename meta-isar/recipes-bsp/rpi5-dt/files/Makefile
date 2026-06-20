.PHONY: build install

build:
	sh ./dtb-build.sh

install:
	mkdir -p $(DESTDIR)$(PREFIX)/usr/lib/rpi5-dt
	cp -av out/* $(DESTDIR)$(PREFIX)/usr/lib/rpi5-dt/
