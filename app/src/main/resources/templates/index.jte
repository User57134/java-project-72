@import hexlet.code.dto.urls.UrlAddingResult

@param UrlAddingResult resultPage

@template.layout.page(
content = @`
<%-- Содержимое страницы --%>
    @if (resultPage != null)
        @if (!resultPage.getFlash().isSuccessed())
            <div class="max-w-7xl mx-auto mb-4 px-4" role="alert">
                <div class="bg-red-100 border border-red-200 text-red-900 px-4 py-3 rounded-xl shadow-sm flex items-center gap-2">
                    <h6 class="font-medium text-sm sm:text-base">
                        ${resultPage.getFlash().getMessage()}
                    </h6>
                </div>
            </div>
        @else
            <div class="max-w-7xl mx-auto mb-4 px-4" role="alert">
                <div class="bg-green-100 border border-green-200 text-green-800 px-4 py-3 rounded-xl shadow-sm flex items-center gap-2">
                    <h6 class="font-medium text-sm sm:text-base">
                        ${resultPage.getFlash().getMessage()}
                    </h6>
                </div>
            </div>
        @endif
    @endif

    <div class="mx-auto max-w-3xl rounded-lg border border-gray-200 bg-gray-50 p-10">
        <h1 class="text-5xl font-light">Анализатор страниц</h1>
        <p class="mt-2 mb-6 text-xl text-gray-600">Бесплатно проверяйте сайты на SEO пригодность</p>
        <form action="/urls" method="post" class="flex flex-wrap items-start gap-3">
            <div class="grow">
                <label for="url-name" class="sr-only">Url для проверки</label>
                @if ((resultPage != null) && (resultPage.getInput() != null))
                    <input
                            id="url-name"
                            type="text"
                            name="url"
                            class="w-full rounded text-lg"
                            placeholder="https://www.example.com"
                            value="${resultPage.getInput()}"
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