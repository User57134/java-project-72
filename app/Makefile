.PHONY: build

build:
	./gradlew build

setup: build-css
	./gradlew installDist

build-css:
	cd tailwind && npm ci
	cd tailwind && npm run build:css