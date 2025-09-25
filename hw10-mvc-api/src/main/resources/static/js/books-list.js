;(async function loadBooks() {
    const tbody = document.getElementById('booksTableBody');
    tbody.innerHTML = '';
    const res = await fetch('/api/v1/books');
    if (!res.ok) {
        tbody.innerHTML = '<tr><td colspan="5">Failed to load</td></tr>';
        return;
    }
    const data = await res.json();

    for (const b of data) {
        const tr = document.createElement('tr');

        const tdId = document.createElement('td');
        tdId.textContent = b.id;

        const tdTitle = document.createElement('td');
        const a = document.createElement('a');
        a.href = `/books/${b.id}`;
        a.textContent = b.title ?? '';
        tdTitle.appendChild(a);

        const tdAuthor = document.createElement('td');
        tdAuthor.textContent = (b.author?.fullName) ?? '';

        const tdGenres = document.createElement('td');
        tdGenres.textContent = (b.genres ?? []).map(g => g.name).join(', ');

        const tdActions = document.createElement('td');
        tdActions.className = 'actions';
        const edit = document.createElement('a');
        edit.className = 'btn';
        edit.href = `/books/${b.id}/edit`;
        edit.textContent = 'Edit';
        const del = document.createElement('button');
        del.className = 'btn danger';
        del.type = 'button';
        del.dataset.id = b.id;
        del.textContent = 'Delete';
        tdActions.append(edit, del);

        tr.append(tdId, tdTitle, tdAuthor, tdGenres, tdActions);
        tbody.appendChild(tr);
    }

    tbody.addEventListener('click', async (e) => {
        const btn = e.target.closest('button[data-id]');
        if (!btn) return;
        const id = btn.dataset.id;
        openConfirm(`/books/${id}/delete`);
        document.getElementById('confirmDeleteForm')
            .addEventListener('submit', async function onSubmit(ev) {
                ev.preventDefault();
                closeConfirm();
                const del = await fetch(`/api/v1/books/${id}`, { method: 'DELETE' });
                if (del.status === 204) {
                    await loadBooks();
                }
            }, { once: true });
    });
})()