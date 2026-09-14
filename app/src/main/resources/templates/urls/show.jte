@import hexlet.code.dto.urls.UrlAddingResult
@import hexlet.code.dto.urls.UrlPage

@param UrlPage page

@template.layout.page(
content = @`
<%-- Содержимое страницы --%>
    @if (page.getFlash() != null)
        @if (!page.getFlash().isSuccessed())
            <div class="bg-red-200">
                <h6 class="text-taupe-800">${page.getFlash().getMessage()}</h6>
            </div>
        @else
            <div class="bg-green-200">
                <h6>${page.getFlash().getMessage()}</h6>
            </div>
        @endif
    @endif

    <div class="mx-auto max-w-3xl rounded-lg border border-gray-200 bg-gray-50 p-10">
        <section>

            <h2>Сайт: ${page.getUrl().getName()}</h2>

            <table data-test="url">
                <tbody>
                <tr>
                    <td>ID</td>
                    <td>${page.getUrl().getId()}</td>
                </tr>
                <tr>
                    <td>Имя</td>
                    <td>${page.getUrl().getName()}</td>
                </tr>
                <tr>
                    <td>Дата создания</td>
                    <td>${page.getUrl().getCreatedAt().toString()}</td>
                </tr>
                </tbody>
            </table>
        </section>

        <section>

            <h2>Проверки</h2>

            <form method="post" action="/urls/${page.getUrl().getId()}/checks" >
                <input type="submit" value="Запустить проверку">
            </form>

            <table data-test="checks">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Код ответа</th>
                        <th>Дата создания</th>
                    </tr>
                </thead>

                @if (page.getChecks() != null)
                    <tbody>
                    @for(var check : page.getChecks())
                        <tr>
                            <td>${check.getId()}</td>
                            <td>${check.getStatusCode()}</td>
                            <td>${check.getCreatedAt().toString()}</td>
                        </tr>
                    @endfor
                    </tbody>
                @endif
            </table>
        </section>
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
    Страница сайта
`
)