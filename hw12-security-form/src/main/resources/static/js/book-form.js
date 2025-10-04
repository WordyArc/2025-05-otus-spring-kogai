import {AuthorsApi, BooksApi, GenresApi} from './shared/api.js'
import {clearFieldErrors, fillOptions, qs, setFieldError, showToast} from './shared/ui.js';
import {ApiError} from './shared/http.js';

(async function init() {
    const path = location.pathname;
    const isEdit = /\/books\/\d+\/edit$/.test(path);
    const id = isEdit ? path.split('/')[2] : null;
    qs('#formTitle').textContent = isEdit ? 'Edit book' : 'Create book';

    try {
        const [authors, genres] = await Promise.all([AuthorsApi.list(), GenresApi.list()]);
        fillOptions(qs('#authorId'), authors, 'id', 'fullName', true);
        fillOptions(qs('#genreIds'), genres, 'id', 'name', false);
    } catch (e) {
        showToast('Failed to load references');
        return;
    }

    if (isEdit) {
        try {
            const data = await BooksApi.get(id);
            qs('#bookId').value = data.id;
            qs('#title').value = data.title ?? '';
            qs('#authorId').value = String(data.author.id);
            const set = new Set((data.genres ?? []).map(g => String(g.id)));
            [...qs('#genreIds').options].forEach(o => { o.selected = set.has(String(o.value)); });
        } catch {
            showToast('Failed to load book');
            return;
        }
    }

    qs('#bookForm').addEventListener('submit', onSubmit);

    function collect() {
        const title = qs('#title').value.trim();
        const authorValue = qs('#authorId').value;
        const authorId = authorValue ? Number(authorValue) : null;
        const genreIds = [...qs('#genreIds').selectedOptions].map(o => Number(o.value));
        return { title, authorId, genreIds };
    }

    function validateLocal({ title, authorId, genreIds }) {
        const errors = {};
        if (!title) errors.title = 'Title must not be blank';
        if (authorId == null) errors.authorId = 'Author is required';
        if (!Array.isArray(genreIds) || genreIds.length === 0) errors.genreIds = 'Select at least one genre';
        return errors;
    }

    function applyErrors(errors) {
        clearFieldErrors(['errTitle','errAuthor','errGenres']);
        if (errors.title)    setFieldError('errTitle',  errors.title);
        if (errors.authorId) setFieldError('errAuthor', errors.authorId);
        if (errors.genreIds) setFieldError('errGenres', errors.genreIds);
    }

    function parseFieldErrors(detail) {
        const map = {};
        (detail || '')
            .split(';')
            .map(s => s.trim())
            .filter(Boolean)
            .forEach(pair => {
                const idx = pair.indexOf(':');
                if (idx === -1) return;
                const field = pair.slice(0, idx).trim();
                const msg = pair.slice(idx + 1).trim();
                map[field] = msg;
            });
        return map;
    }

    async function onSubmit(e) {
        e.preventDefault();
        clearFieldErrors(['errTitle','errAuthor','errGenres']);

        const dto = collect();
        const localErrors = validateLocal(dto);
        if (Object.keys(localErrors).length) {
            applyErrors(localErrors);
            return;
        }

        try {
            const saved = isEdit ? await BooksApi.update(id, dto) : await BooksApi.create(dto);
            location.assign(`/books/${saved.id}`);
        } catch (err) {
            if (!(err instanceof ApiError)) {
                showToast('Save failed');
                return;
            }
            if (err.status === 400) {
                const fe = parseFieldErrors(err.detail);
                applyErrors({
                    title:    fe.title,
                    authorId: fe.authorId,
                    genreIds: fe.genreIds
                });
                if (!fe.title && !fe.authorId && !fe.genreIds) showToast(err.detail);
                return;
            }
            if (err.status === 404) {
                const m = err.detail.toLowerCase();
                const mapped = {};
                if (m.includes('author')) mapped.authorId = err.detail;
                if (m.includes('genre'))  mapped.genreIds = err.detail;
                if (Object.keys(mapped).length) {
                    applyErrors(mapped);
                    return;
                }
            }
            showToast(err.detail || 'Save failed');
        }
    }
})();