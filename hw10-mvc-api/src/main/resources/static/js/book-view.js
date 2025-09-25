(async function init() {
    const id = location.pathname.split('/')[2];
    const bookRes = await fetch(`/api/v1/books/${id}`);
    if (!bookRes.ok) return;
    const book = await bookRes.json();

    document.getElementById('bookTitle').textContent = book.title ?? '';
    document.getElementById('bookAuthor').textContent = book.author?.fullName ?? '';
    document.getElementById('bookGenres').textContent = (book.genres ?? []).map(g => g.name).join(', ');
    document.getElementById('editLink').href = `/books/${id}/edit`;

    document.getElementById('btnDelete').addEventListener('click', () => {
        openConfirm(`/books/${id}/delete`);
        document.getElementById('confirmDeleteForm')
            .addEventListener('submit', async function onSubmit(e) {
                e.preventDefault();
                closeConfirm();
                const del = await fetch(`/api/v1/books/${id}`, { method: 'DELETE' });
                if (del.status === 204) location.assign('/books');
            }, { once: true });
    });

    const res = await fetch(`/api/v1/books/${id}/comments`);
    if (!res.ok) return;
    const list = await res.json();
    if (list.length) {
        const ul = document.getElementById('commentsList');
        ul.style.display = 'block';
        document.getElementById('commentsEmpty').style.display = 'none';
        for (const c of list) {
            const li = document.createElement('li');
            const div = document.createElement('div');
            div.textContent = c.text ?? '';
            const small = document.createElement('small');
            small.textContent = c.createdAt ?? '';
            li.appendChild(div);
            li.appendChild(small);
            ul.appendChild(li);
        }
    }
})();