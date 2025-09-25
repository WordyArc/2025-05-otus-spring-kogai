;(function () {
    const tbody = document.getElementById('booksTableBody');
    const rowTpl = document.getElementById('book-row-template');
    const emptyTpl = document.getElementById('empty-row-template');

    tbody.addEventListener('click', async (e) => {
        const btn = e.target.closest('.delete-btn[data-id]');
        if (!btn) return;

        const id = btn.dataset.id;
        openConfirm(`/books/${id}/delete`);

        document.getElementById('confirmDeleteForm')
            .addEventListener('submit', async function onSubmit(ev) {
                ev.preventDefault();
                closeConfirm();
                const resp = await fetch(`/api/v1/books/${id}`, { method: 'DELETE' });
                if (resp.status === 204) {
                    await loadBooks();
                }
            }, { once: true });
    });

    async function loadBooks() {
        tbody.replaceChildren();

        const res = await fetch('/api/v1/books');
        if (!res.ok) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 5;
            td.textContent = 'Failed to load';
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        const books = await res.json();
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
            row.querySelector('.cell-author').textContent = (b.author?.fullName) ?? '';
            row.querySelector('.cell-genres').textContent = (b.genres ?? []).map(g => g.name).join(', ');

            const edit = row.querySelector('.edit-link');
            edit.href = `/books/${b.id}/edit`;

            const del = row.querySelector('.delete-btn');
            del.dataset.id = String(b.id);

            frag.appendChild(row);
        }

        tbody.appendChild(frag);
    }

    loadBooks();
})();