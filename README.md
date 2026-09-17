# Анализатор страниц (Java)

[![hexlet-check](https://github.com/User57134/java-project-72/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/User57134/java-project-72/actions)
[![Analyze](https://github.com/User57134/java-project-72/actions/workflows/analyze.yml/badge.svg)](https://github.com/User57134/java-project-72/actions/workflows/analyze.yml)

Веб-приложение для анализа страниц на SEO-пригодность. Позволяет запускать проверки сайтов анализируя коды ответа, заголовоки title (\<title>), теги h1(\<h1>) и meta-описание (\<meta name="description">).


Демо: https://java-project-72-51b4.onrender.com/


## Функциональность

- Добавление URL для анализа
- Проверка HTTP статуса
- Анализ тегов: title, h1, description
- Сохранение истории проверок для каждого URL
- Просмотр всех добавленных URL


## Используемые технологии

| Назначение | Технология |
|----------|-------------|
| **Язык программирования** | Java 21 |
| **Web фреймворк** | Javalin 7 |
| **Шаблонизатор** | JTE |
| **Базы данных** | H2, PostgreSQL |
| **Пул JDBC-соединений** | HikariCP |
| **Библиотека для отправки запросов HTTP** | Unirest |
| **Библиотека для разбора HTML** | Jsoup |
| **Система сборки** | Gradle |
| **CI/CD** | GitHub Actions |
| **Тесты** | JUnit 5, MockWebServer |


## Установка

<!-- Опишите установку: клонирование, зависимости, переменные окружения -->

```bash
git clone https://github.com/User57134/java-project-72.git
cd java-project-72/app
make setup
```


## Использование

<!-- Добавьте примеры запуска и запись asciinema — именно это смотрит работодатель -->

```bash
make start
```
