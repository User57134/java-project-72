@import hexlet.code.dto.urls.UrlsPage

@param UrlsPage page

@template.layout.page(
content = @`
<%-- Содержимое страницы --%>
    <div class="mx-auto max-w-3xl rounded-lg border border-gray-200 bg-gray-50 p-10">
        <table data-test="urls">
            <thead>
            <tr>
                <th>ID</th>
                <th>Имя</th>
                <th>Дата создания</th>
            </tr>
            </thead>

            @if (!page.getUrls().isEmpty())
                <tbody>
                @for(var url : page.getUrls())
                    <tr>
                        <td>${url.getId()}</td>
                        <td><a href="/urls/${url.getId()}">${url.getName()}</a></td>
                        <td>${url.getCreatedAt().toString()}</td>
                    </tr>
                @endfor
                </tbody>
            @endif
        </table>
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
    Сайты
`
)