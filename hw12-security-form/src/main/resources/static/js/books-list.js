import {BooksApi} from './shared/api.js'
import {qs} from './shared/ui.js';

;(function () {
    const tbody = qs('#booksTableBody');
    const rowTpl = qs('#book-row-template');
    const emptyTpl = qs('#empty-row-template');

    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('.delete-btn[data-id]');
        if (!btn) return;

        const id = btn.dataset.id;
        openConfirm(`/books/${id}/delete`);

        document.getElementById('confirmDeleteForm')
            .addEventListener('submit', async function onSubmit(ev) {
                ev.preventDefault();
                closeConfirm();
                await BooksApi.remove(id);
                await loadBooks();
            }, {once: true});
    });

    async function loadBooks() {
        tbody.replaceChildren();
        try {
            const books = await BooksApi.list();
            if (!books.length) {
                tbody.appendChild(emptyTpl.content.cloneNode(true));
                return;
            }
            const frag = document.createDocumentFragment();
            for (const b of books) {
                const row = rowTpl.content.cloneNode(true);
                row.querySelector('.cell-id').textContent = b.id;
                row.querySelector('.link-title').textContent = b.title ?? '';
                row.querySelector('.link-title').href = `/books/${b.id}`;
                row.querySelector('.cell-author').textContent = b.author?.fullName ?? '';
                row.querySelector('.cell-genres').textContent = (b.genres ?? []).map(g => g.name).join(', ');
                row.querySelector('.edit-link').href = `/books/${b.id}/edit`;
                row.querySelector('.delete-btn').dataset.id = String(b.id);
                frag.appendChild(row);
            }
            tbody.appendChild(frag);
        } catch {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 5;
            td.textContent = 'Failed to load';
            tr.appendChild(td);
            tbody.appendChild(tr);
        }
    }

    loadBooks();
})();
