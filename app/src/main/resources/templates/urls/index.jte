@import hexlet.code.util.SystemDateTime
@import hexlet.code.dto.urls.UrlsPage


@param UrlsPage page

@template.layout.page(
content = @`
<%-- Содержимое страницы --%>
    <div class="mx-auto max-w-5xl rounded-lg border border-gray-200 bg-gray-50 p-10">
        <table data-test="urls" class="w-full border-collapse border border-gray-300">
            <thead>
            <tr class="bg-gray-100">
                <th class="border border-gray-300 px-4 py-2 text-left">ID</th>
                <th class="border border-gray-300 px-4 py-2 text-left">Имя</th>
                <th class="border border-gray-300 px-4 py-2 text-left">Последняя проверка</th>
                <th class="border border-gray-300 px-4 py-2 text-left">Код ответа</th>
            </tr>
            </thead>

            @if (!page.getUrls().isEmpty())
                <tbody>
                @for(var url : page.getUrls())
                    <tr>
                        <td class="border border-gray-300 px-4 py-2">${url.getId()}</td>
                        <td class="border border-gray-300 px-4 py-2"><a href="/urls/${url.getId()}" class="text-amber-950 hover:text-amber-600 transition-colors duration-200 font-medium">${url.getName()}</a></td>
                        @if (page.getLastCheckForUrl(url.getId()) != null)
                            <td class="border border-gray-300 px-4 py-2">${SystemDateTime.format(page.getLastCheckForUrl(url.getId()).getCreatedAt())}</td>
                            <td class="border border-gray-300 px-4 py-2">${page.getLastCheckForUrl(url.getId()).getStatusCode()}</td>
                        @else
                            <td class="border border-gray-300 px-4 py-2"></td>
                            <td class="border border-gray-300 px-4 py-2"></td>
                        @endif
                    </tr>
                @endfor
                </tbody>
            @endif
        </table>
    </div>

`
,
title = @`Сайты`)