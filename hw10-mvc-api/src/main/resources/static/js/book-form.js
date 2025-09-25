(async function init() {
    const path = location.pathname;
    const isEdit = /\/books\/\d+\/edit$/.test(path);
    const id = isEdit ? path.split('/')[2] : null;
    document.getElementById('formTitle').textContent = isEdit ? 'Edit book' : 'Create book';

    const [authorsRes, genresRes] = await Promise.all([
        fetch('/api/v1/authors'),
        fetch('/api/v1/genres')
    ]);
    if (!authorsRes.ok || !genresRes.ok) {
        alert('Failed to load references');
        return;
    }
    const authors = await authorsRes.json();
    const genres = await genresRes.json();
    fillOptions(document.getElementById('authorId'), authors, 'id', 'fullName', true);
    fillOptions(document.getElementById('genreIds'), genres, 'id', 'name', false);

    if (isEdit) {
        const resp = await fetch(`/api/v1/books/${id}`);
        if (!resp.ok) return;
        const data = await resp.json();
        document.getElementById('bookId').value = data.id;
        document.getElementById('title').value = data.title ?? '';
        document.getElementById('authorId').value = String(data.author.id);
        const set = new Set((data.genres ?? []).map(g => String(g.id)));
        [...document.getElementById('genreIds').options].forEach(o => { o.selected = set.has(o.value); });
    }

    document.getElementById('bookForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        clearErrors();
        const payload = collect();
        const url = isEdit ? `/api/v1/books/${id}` : '/api/v1/books';
        const method = isEdit ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            const body = await res.json();
            location.assign(`/books/${body.id}`);
        } else if (res.status === 400) {
            const pd = await res.json();
            showErrors(pd.detail ?? 'Validation error');
        } else {
            alert('Save failed');
        }
    });

    function collect() {
        const title = document.getElementById('title').value.trim();
        const authorId = Number(document.getElementById('authorId').value);
        const genreIds = [...document.getElementById('genreIds').selectedOptions].map(o => Number(o.value));
        return { title, authorId, genreIds };
    }

    function fillOptions(sel, items, valKey, textKey, addPlaceholder) {
        sel.innerHTML = '';
        if (addPlaceholder) {
            const opt = document.createElement('option');
            opt.value = '';
            opt.disabled = true;
            opt.selected = true;
            opt.textContent = '- select -';
            sel.appendChild(opt);
        }
        for (const it of items) {
            const opt = document.createElement('option');
            opt.value = it[valKey];
            opt.textContent = it[textKey];
            sel.appendChild(opt);
        }
    }

    function showErrors(msg) {
        const m = String(msg).toLowerCase();
        if (m.includes('title')) show('errTitle', msg);
        if (m.includes('author')) show('errAuthor', msg);
        if (m.includes('genre')) show('errGenres', msg);
    }

    function show(id, text) {
        const el = document.getElementById(id);
        el.textContent = text;
        el.style.display = 'block';
    }

    function clearErrors() {
        ['errTitle', 'errAuthor', 'errGenres'].forEach(id => {
            const el = document.getElementById(id);
            el.textContent = '';
            el.style.display = 'none';
        });
    }
})();