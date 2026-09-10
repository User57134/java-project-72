@import hexlet.code.dto.urls.UrlAddingResult

@param UrlAddingResult result

@template.layout.page(
content = @`
<%-- Содержимое страницы --%>
    @if (result != null)
        @if (!result.isSuccessed())
            <div class="bg-red-200">
                <h6 class="text-taupe-800">${result.getFlash()}</h6>
            </div>
        @else
            <div class="bg-green-200">
                <h6>${result.getFlash()}</h6>
            </div>
        @endif
    @endif

    <div class="mx-auto max-w-3xl rounded-lg border border-gray-200 bg-gray-50 p-10">
        <h1 class="text-5xl font-light">Анализатор страниц</h1>
        <p class="mt-2 mb-6 text-xl text-gray-600">Бесплатно проверяйте сайты на SEO пригодность</p>
        <form action="/urls" method="post" class="flex flex-wrap items-start gap-3">
            <div class="grow">
                <label for="url-name" class="sr-only">Url для проверки</label>
                @if (result != null)
                    <input
                            id="url-name"
                            type="text"
                            name="url"
                            class="w-full rounded text-lg"
                            placeholder="https://www.example.com"
                            value="${result.getInput()}"
                    >
                @else
                    <input
                            id="url-name"
                            type="text"
                            name="url"
                            class="w-full rounded text-lg"
                            placeholder="https://www.example.com"
                    >
                @endif
            </div>
            <input type="submit"
                   class="cursor-pointer rounded bg-blue-600 px-8 py-2 text-lg font-medium text-white uppercase hover:bg-blue-700"
                   value="Проверить">
        </form>
    </div>

`
,
footer = @`
<%-- Содержимое нижнего колонтитула --%>
    <span>Все права защищены. ©</span>
`
,
title = @`
<%-- Содержимое заголовка страницы --%>
    Главная
`
)