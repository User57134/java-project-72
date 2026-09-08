@import gg.jte.Content


@param Content content
@param Content footer = null
@param Content title = null


<!DOCTYPE html>
<html lang="en">
<head>
    <title>
        @if (title != null)
            ${title}
        @else
            Анализатор страниц
        @endif
    </title>
    <meta charset='utf-8'>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="/css/main.css" rel="stylesheet">
</head>
<body>
<nav>
    <ul>
        <li><a href="/">Главная</a></li>
        <li><a href="/urls">Сайты</a></li>
    </ul>
</nav>

<main>
    <div>
        Alert
    </div>

    ${content}

</main>

<footer>
    @if (footer != null)
        ${footer}
    @endif
</footer>
</body>
</html>
