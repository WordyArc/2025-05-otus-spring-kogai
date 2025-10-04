import {request} from './http.js';

const base = '/api/v1/books';

export const BooksApi = {
    list() {
        return request(base);
    },
    get(id) {
        return request(`${base}/${id}`);
    },
    create(dto) {
        return request(base, {method: 'POST', body: dto});
    },
    update(id, dto) {
        return request(`${base}/${id}`, {method: 'PUT', body: dto});
    },
    patch(id, patch) {
        return request(`${base}/${id}`, {method: 'PATCH', body: patch});
    },
    remove(id) {
        return request(`${base}/${id}`, {method: 'DELETE'});
    },
};

export const AuthorsApi = {
    list() {
        return request('/api/v1/authors');
    }
};

export const GenresApi = {
    list() {
        return request('/api/v1/genres');
    }
};

export const CommentsApi = {
    listByBook(bookId) {
        return request(`/api/v1/books/${bookId}/comments`);
    },
};
