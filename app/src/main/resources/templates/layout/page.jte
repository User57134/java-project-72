@import gg.jte.Content
@import hexlet.code.dto.Flash


@param Content content
@param Content footer = null
@param Content title = null
@param Flash flash = null


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
<body class="min-h-screen flex flex-col justify-between">
    <nav class="bg-slate-900 border-b border-slate-800 px-10 py-4">
        <ul class="max-w-7xl mx-auto flex flex-row justify-between items-center text-sm font-medium">
            <li><a href="/" class="text-white hover:text-indigo-400 transition-colors duration-200 py-2">Анализатор страниц</a></li>
            <li><a href="/urls" class="text-slate-400 hover:text-white transition-colors duration-200 py-2">Сайты</a></li>
        </ul>
    </nav>

    <main>
        @if (flash != null && flash.isActive())
            <div class="max-w-7xl mx-auto mb-4 px-4" role="alert">
                <div class="${flash.isSucceeded() ? "bg-green-100 border-green-200 text-green-800" : "bg-red-100 border-red-200 text-red-900"} border px-4 py-3 rounded-xl shadow-sm flex items-center gap-2">
                    <h6 class="font-medium text-sm sm:text-base">
                        ${flash.getMessage()}
                    </h6>
                </div>
            </div>
        @endif

        ${content}
    </main>

    <footer class="bg-slate-50 text-slate-500 py-6 px-4 border-t border-slate-200">
        <div class="max-w-7xl mx-auto">
            <div class="flex flex-col sm:flex-row justify-between gap-4 text-xs font-medium">
                <div>
                    &copy; 2026 Анализатор Страниц. Все права защищены.
                </div>
            </div>
        </div>
    </footer>
</body>
</html>
