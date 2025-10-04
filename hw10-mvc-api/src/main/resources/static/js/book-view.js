import {BooksApi, CommentsApi} from './shared/api.js'
import {qs, showToast} from './shared/ui.js';

(async function init() {
    const id = location.pathname.split('/')[2];

    try {
        const book = await BooksApi.get(id);
        qs('#bookTitle').textContent = book.title ?? '';
        qs('#bookAuthor').textContent = book.author?.fullName ?? '';
        qs('#bookGenres').textContent = (book.genres ?? []).map(g => g.name).join(', ');
        qs('#editLink').href = `/books/${id}/edit`;
    } catch {
        showToast('Failed to load book');
        return;
    }

    qs('#btnDelete').addEventListener('click', () => {
        openConfirm(`/books/${id}/delete`);
        document.getElementById('confirmDeleteForm')
            .addEventListener('submit', async function onSubmit(e) {
                e.preventDefault();
                closeConfirm();
                try {
                    await BooksApi.remove(id);
                    location.assign('/books');
                } catch {
                    showToast('Delete failed');
                }
            }, {once: true});
    });

    try {
        const list = await CommentsApi.listByBook(id);
        if (list.length) {
            const ul = qs('#commentsList');
            ul.style.display = 'block';
            qs('#commentsEmpty').style.display = 'none';
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
    } catch {
    }
})();