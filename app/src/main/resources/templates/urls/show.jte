@import hexlet.code.dto.urls.UrlAddingResult
@import hexlet.code.dto.urls.UrlPage

@param UrlPage  page
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
    Сайт
`
)