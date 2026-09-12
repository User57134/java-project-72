.PHONY: build

clean:
	./gradlew clean

build:
	./gradlew clean build

setup: build-css
	./gradlew installDist

build-css:
	cd tailwind && npm ci
	cd tailwind && npm run build:css

test:
	./gradlew test

lint:
	./gradlew spotlessCheck

start:
	npx concurrently --kill-others --names "css,app" --prefix-colors "blue,green" "npm run watch:css --prefix tailwind" "APP_ENV=development ./gradlew run"
