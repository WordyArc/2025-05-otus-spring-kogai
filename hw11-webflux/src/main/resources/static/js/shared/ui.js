export const qs  = (sel, root = document) => root.querySelector(sel);
export const qsa = (sel, root = document) => [...root.querySelectorAll(sel)];

export function fillOptions(selectEl, items, valueKey, textKey, addPlaceholder = false) {
    selectEl.innerHTML = '';
    if (addPlaceholder) {
        const opt = document.createElement('option');
        opt.value = '';
        opt.disabled = true;
        opt.selected = true;
        opt.textContent = '- select -';
        selectEl.appendChild(opt);
    }
    for (const it of items) {
        const opt = document.createElement('option');
        opt.value = it[valueKey];
        opt.textContent = it[textKey];
        selectEl.appendChild(opt);
    }
}

export function setFieldError(id, text) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = text;
    el.style.display = text ? 'block' : 'none';
}

export function clearFieldErrors(ids) {
    ids.forEach(id => setFieldError(id, ''));
}

export function showToast(msg) {
    alert(msg);
}