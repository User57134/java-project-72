@import hexlet.code.dto.Flash
@import hexlet.code.util.Formatter
@import hexlet.code.util.CorrectDisplay
@import hexlet.code.dto.urls.UrlPage

@param UrlPage page
@param Flash flash

@template.layout.page(
flash = flash,
content = @`
<%-- Содержимое страницы --%>
        <div class="mx-auto max-w-5xl rounded-lg border border-gray-200 bg-gray-50 p-10">
        <section>
            <h1 class="text-3xl font-serif font-bold text-slate-800 tracking-tight mb-4">
                Сайт: <span class="font-medium text-blue-900">${page.getUrl().getName()}</span>
            </h1>

            <table data-test="url" class="text-ellipsis w-full border-collapse border border-gray-300">
                <tbody>
                <tr>
                    <td class="border border-gray-300 px-4 py-2 font-bold">ID</td>
                    <td class="border border-gray-300 px-4 py-2">${page.getUrl().getId()}</td>
                </tr>
                <tr>
                    <td class="border border-gray-300 px-4 py-2 font-bold">Имя</td>
                    <td class="border border-gray-300 px-4 py-2">${page.getUrl().getName()}</td>
                </tr>
                <tr>
                    <td class="border border-gray-300 px-4 py-2 font-bold">Дата создания</td>
                    <td class="border border-gray-300 px-4 py-2">${Formatter.makeTimestampAsString(page.getUrl().getCreatedAt())}</td>
                </tr>
                </tbody>
            </table>
        </section>

        <section>
            <h2 class="text-2xl font-bold text-slate-700 tracking-tight mb-4 mt-4">Проверки</h2>

            <form method="post" action="/urls/${page.getUrl().getId()}/checks">
                <input type="submit" value="Запустить проверку"
                       class="w-full bg-slate-800 hover:bg-slate-700 active:bg-slate-900 text-white font-semibold py-2.5 px-4 rounded-xl shadow-sm transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-slate-500 focus:ring-offset-2 disabled:opacity-40 disabled:cursor-not-allowed">
            </form>

            <table data-test="checks" class="w-full border-collapse border border-gray-300">
                <thead>
                <tr class="bg-gray-100">
                    <th class="border border-gray-300 px-4 py-2">ID</th>
                    <th class="border border-gray-300 px-4 py-2">Код ответа</th>
                    <th class="border border-gray-300 px-4 py-2">h1</th>
                    <th class="border border-gray-300 px-4 py-2">title</th>
                    <th class="border border-gray-300 px-4 py-2">description</th>
                    <th class="border border-gray-300 px-4 py-2">Дата создания</th>
                </tr>
                </thead>

                @if (page.getChecks() != null)
                    <tbody>
                    @for(var check : page.getChecks())
                        <tr>
                            <td class="border border-gray-300 px-4 py-2">${check.getId()}</td>
                            <td class="border border-gray-300 px-4 py-2">${check.getStatusCode()}</td>
                            <td class="border border-gray-300 px-4 py-2">${CorrectDisplay.apply(check.getH1())}</td>
                            <td class="border border-gray-300 px-4 py-2">${CorrectDisplay.apply(check.getTitle())}</td>
                            <td class="border border-gray-300 px-4 py-2">${CorrectDisplay.apply(check.getDescription())}</td>
                            <td class="border border-gray-300 px-4 py-2">${Formatter.makeTimestampAsString(check.getCreatedAt())}</td>
                        </tr>
                    @endfor
                    </tbody>
                @endif
            </table>
        </section>
    </div>

`
,
title = @`Страница сайта`)