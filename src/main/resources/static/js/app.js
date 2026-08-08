const state = {
    page: 0,
    size: 10,
    totalPages: 0,
    records: [],
    editingId: null
};

const elements = {
    podName: document.querySelector('#instance-heading'),
    podIp: document.querySelector('#pod-ip'),
    nodeName: document.querySelector('#node-name'),
    servedAt: document.querySelector('#served-at'),
    refreshInstance: document.querySelector('#refresh-instance'),
    form: document.querySelector('#record-form'),
    formHeading: document.querySelector('#form-heading'),
    title: document.querySelector('#record-title'),
    content: document.querySelector('#record-content'),
    submit: document.querySelector('#submit-record'),
    cancelEdit: document.querySelector('#cancel-edit'),
    formStatus: document.querySelector('#form-status'),
    recordList: document.querySelector('#record-list'),
    totalRecords: document.querySelector('#total-records'),
    pageSize: document.querySelector('#page-size'),
    firstPage: document.querySelector('#first-page'),
    previousPage: document.querySelector('#previous-page'),
    nextPage: document.querySelector('#next-page'),
    lastPage: document.querySelector('#last-page'),
    pageLabel: document.querySelector('#page-label')
};

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
});

async function request(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            Accept: 'application/json',
            ...(options.body ? {'Content-Type': 'application/json'} : {}),
            ...options.headers
        }
    });

    if (response.status === 204) {
        return null;
    }

    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        const message = payload && payload.message ? payload.message : `请求失败（${response.status}）`;
        throw new Error(message);
    }
    return payload;
}

async function loadInstance() {
    elements.refreshInstance.disabled = true;
    try {
        const instance = await request(`/api/instance?request=${Date.now()}`, {cache: 'no-store'});
        elements.podName.textContent = instance.podName;
        elements.podIp.textContent = instance.podIp;
        elements.nodeName.textContent = instance.nodeName;
        elements.servedAt.textContent = formatDate(instance.servedAt);
    } catch (error) {
        elements.podName.textContent = '实例信息读取失败';
        elements.podIp.textContent = '-';
        elements.nodeName.textContent = '-';
        elements.servedAt.textContent = '-';
    } finally {
        elements.refreshInstance.disabled = false;
    }
}

async function loadRecords() {
    renderLoading();
    try {
        const result = await request(`/api/records?page=${state.page}&size=${state.size}`);
        state.records = result.content;
        state.totalPages = result.totalPages;
        renderRecords(result);
    } catch (error) {
        state.records = [];
        state.totalPages = 0;
        renderTableMessage(error.message);
        updatePagination(0, 0, true, true);
    }
}

function renderLoading() {
    renderTableMessage('正在读取记录...');
}

function renderTableMessage(message) {
    elements.recordList.replaceChildren();
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 4;
    cell.className = 'empty-state';
    cell.textContent = message;
    row.append(cell);
    elements.recordList.append(row);
}

function renderRecords(result) {
    elements.recordList.replaceChildren();
    elements.totalRecords.textContent = result.totalElements;

    if (result.content.length === 0) {
        renderTableMessage('暂无记录');
    } else {
        result.content.forEach(record => elements.recordList.append(createRecordRow(record)));
    }

    updatePagination(result.page, result.totalPages, result.first, result.last);
}

function createRecordRow(record) {
    const row = document.createElement('tr');
    row.dataset.recordId = record.id;

    const title = createCell('标题', record.title, 'record-title');
    const content = createCell('内容', record.content, 'record-content');
    const createdAt = createCell('创建时间', formatDate(record.createdAt), 'record-time');
    const actions = document.createElement('td');
    actions.dataset.label = '操作';

    const actionGroup = document.createElement('div');
    actionGroup.className = 'record-actions';

    const editButton = document.createElement('button');
    editButton.type = 'button';
    editButton.className = 'action-button';
    editButton.textContent = '编辑';
    editButton.addEventListener('click', () => startEditing(record));

    const deleteButton = document.createElement('button');
    deleteButton.type = 'button';
    deleteButton.className = 'action-button danger';
    deleteButton.textContent = '删除';
    deleteButton.addEventListener('click', () => deleteRecord(record));

    actionGroup.append(editButton, deleteButton);
    actions.append(actionGroup);
    row.append(title, content, createdAt, actions);
    return row;
}

function createCell(label, value, className) {
    const cell = document.createElement('td');
    cell.dataset.label = label;
    cell.className = className;
    cell.textContent = value;
    return cell;
}

function updatePagination(page, totalPages, first, last) {
    elements.pageLabel.textContent = totalPages === 0 ? '0 / 0' : `${page + 1} / ${totalPages}`;
    elements.firstPage.disabled = first || totalPages === 0;
    elements.previousPage.disabled = first || totalPages === 0;
    elements.nextPage.disabled = last || totalPages === 0;
    elements.lastPage.disabled = last || totalPages === 0;
}

function startEditing(record) {
    state.editingId = record.id;
    elements.formHeading.textContent = '修改记录';
    elements.submit.textContent = '保存修改';
    elements.cancelEdit.hidden = false;
    elements.title.value = record.title;
    elements.content.value = record.content;
    setFormStatus('');
    elements.title.focus();
    elements.formHeading.scrollIntoView({behavior: 'smooth', block: 'center'});
}

function stopEditing() {
    state.editingId = null;
    elements.form.reset();
    elements.formHeading.textContent = '添加记录';
    elements.submit.textContent = '添加记录';
    elements.cancelEdit.hidden = true;
    setFormStatus('');
}

async function submitRecord(event) {
    event.preventDefault();
    if (!elements.form.reportValidity()) {
        return;
    }

    const payload = {
        title: elements.title.value,
        content: elements.content.value
    };
    const editing = state.editingId !== null;
    const url = editing ? `/api/records/${state.editingId}` : '/api/records';
    const method = editing ? 'PUT' : 'POST';

    elements.submit.disabled = true;
    elements.cancelEdit.disabled = true;
    setFormStatus(editing ? '正在保存修改...' : '正在添加记录...');

    try {
        await request(url, {method, body: JSON.stringify(payload)});
        stopEditing();
        if (!editing) {
            state.page = 0;
        }
        setFormStatus(editing ? '记录已修改' : '记录已添加', 'success');
        await loadRecords();
    } catch (error) {
        setFormStatus(error.message, 'error');
    } finally {
        elements.submit.disabled = false;
        elements.cancelEdit.disabled = false;
    }
}

async function deleteRecord(record) {
    if (!window.confirm(`确认删除“${record.title}”吗？`)) {
        return;
    }

    try {
        await request(`/api/records/${record.id}`, {method: 'DELETE'});
        if (state.editingId === record.id) {
            stopEditing();
        }
        if (state.records.length === 1 && state.page > 0) {
            state.page -= 1;
        }
        setFormStatus('记录已删除', 'success');
        await loadRecords();
    } catch (error) {
        setFormStatus(error.message, 'error');
    }
}

function setFormStatus(message, type = '') {
    elements.formStatus.textContent = message;
    elements.formStatus.className = `status-message${type ? ` ${type}` : ''}`;
}

function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '-' : dateFormatter.format(date);
}

elements.refreshInstance.addEventListener('click', loadInstance);
elements.form.addEventListener('submit', submitRecord);
elements.cancelEdit.addEventListener('click', stopEditing);
elements.pageSize.addEventListener('change', () => {
    state.size = Number(elements.pageSize.value);
    state.page = 0;
    loadRecords();
});
elements.firstPage.addEventListener('click', () => {
    state.page = 0;
    loadRecords();
});
elements.previousPage.addEventListener('click', () => {
    state.page = Math.max(0, state.page - 1);
    loadRecords();
});
elements.nextPage.addEventListener('click', () => {
    state.page = Math.min(state.totalPages - 1, state.page + 1);
    loadRecords();
});
elements.lastPage.addEventListener('click', () => {
    state.page = Math.max(0, state.totalPages - 1);
    loadRecords();
});

loadInstance();
loadRecords();
