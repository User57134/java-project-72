plugins {

    // Плагин для запуска приложения
    application

    // Плагин для управления версиями подключенных компонентов
    alias(libs.plugins.versions)

    // Анализатор кода (линтер) и форматтер
    alias(libs.plugins.spotless)

    // Плагин для сборки одного jar со всеми ресурсами
    alias(libs.plugins.shadow)

    // Плагин для генерации классов
    alias(libs.plugins.lombok)
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Указание главного класса приложения
application {
    mainClass.set("hexlet.code.App")
}

// Настройка форматтера на использование форматирования кода от Google
spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat().aosp()
        formatAnnotations()
        leadingTabsToSpaces(4)
        endWithNewline()
    }
}

dependencies {
    // Подключение веб-фреймворка Javalin
    implementation(libs.javalin)

    // Подключение фасада для обработки логов совместно с простейшей реализацией
    implementation(libs.slf4j.simple)

    // Подключение пуллера потокво для БД
    implementation(libs.hicariCP)

    // Подключение базы данных H2
    implementation(libs.h2database)

    // Подключаем модуль рендеринга для Javalin,
    // implementation означает, что библиотека будет упакована внутрь вашего готового приложения
    // и будет доступна как при компиляции, так и во время работы
    implementation(libs.javalin.rendering)

    // Подключаем шаблонизатор Jte для Javalin
    implementation(libs.jte)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}