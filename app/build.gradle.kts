import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {

    // Плагин для запуска приложения
    application

    // Плагин для анализа покрытия
    jacoco

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
    implementation(libs.hikariCP)

    // Подключение базы данных H2
    implementation(libs.h2database)

    // Подключаем модуль рендеринга для Javalin-jte,
    // implementation означает, что библиотека будет упакована внутрь вашего готового приложения
    // и будет доступна как при компиляции, так и во время работы
    implementation(libs.javalin.rendering.jte)

    // Подключаем шаблонизатор Jte для Javalin
    implementation(libs.jte)

    // Подключаем библиотеку commons-lang3-lib для обработки чисел
    implementation(libs.commons.lang3)

    // Подключение драйвера Postgresql
    implementation(libs.postgresql)

    // Подключается для тестирования: assertThat
    testImplementation(libs.assertj)

    // Подключается для тестирования Javalin
    testImplementation(libs.javalin.testtool)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.test {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true

        // какие события показывать
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR,
        )

        // формат исключений
        exceptionFormat = TestExceptionFormat.FULL

        // детали
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}


// Точка входа из-под покрытия исключена: у класса с одним main jacoco считает
// ещё и неявный конструктор, который никто не вызывает, и на маленьком проекте
// это одно тянет покрытие вниз.
val coverageExcludes = listOf("hexlet/code/App.class")


fun JacocoReportBase.excludeEntryPoint() {
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExcludes) } }),
    )
}


// Отчёт о покрытии считается сразу после тестов, отдельный вызов не нужен.
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    excludeEntryPoint()
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}


tasks.test { finalizedBy(tasks.jacocoTestReport) }

// Порог покрытия: ниже него `./gradlew build` падает,
// и сборка в CI краснеет вместе с ним.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    excludeEntryPoint()
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }
