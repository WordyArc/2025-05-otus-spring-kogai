export class ApiError extends Error {
    constructor(status, title, detail, raw) {
        super(`${status} ${title}: ${detail}`);
        this.name = 'ApiError';
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.raw = raw;
    }
}

export async function request(url, {method = 'GET', body, headers} = {}) {
    const opts = {method, headers: {'Content-Type': 'application/json', ...(headers || {})}};
    if (body !== undefined) opts.body = typeof body === 'string' ? body : JSON.stringify(body);

    const response = await fetch(url, opts);
    const isJson = (response.headers.get('content-type') || '').includes('application/json');
    const data = isJson ? await response.json().catch(() => null) : null;

    if (response.ok) return data;

    const title = data?.title || response.statusText || 'Error';
    const detail = data?.detail || 'Request failed';
    throw new ApiError(response.status, title, detail, data);
}