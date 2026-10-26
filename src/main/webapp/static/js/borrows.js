/* =========================================================
   借阅记录页专属脚本
   公共逻辑（侧边栏折叠、#year）在 dashboard.js 中，这里不重复

   数据流：页面加载 → GET borrows/list → 渲染表格
           新增/编辑 → Modal → POST borrows/add | borrows/update
           归还     → POST borrows/return?id=
           删除     → POST borrows/delete?id=
   ========================================================= */

/* =========================================================
   ★★★ 后端联调重点区域（一）：接口配置 ★★★
   约定返回格式：
     GET  borrows/list              → { success:true, data:[...] }  或直接返回数组 [...]
     POST borrows/add               → { success:true } / { success:false, message }
     POST borrows/update            → 同上（请求体带 borrowId + 字段）
     POST borrows/return?id=xx      → 同上
     POST borrows/delete?id=xx      → 同上
   ========================================================= */
const API_CONFIG = {
    listUrl:   'borrows/list',
    addUrl:    'borrows/add',
    updateUrl: 'borrows/update',
    returnUrl: 'borrows/return',
    deleteUrl: 'borrows/delete',
};

/** 缓存在页面上的数据（用于编辑时快速取原值） */
let borrowsData = [];

/* =========================================================
   0. 启动
   ========================================================= */
document.addEventListener('DOMContentLoaded', function () {
    const btnAdd   = document.getElementById('btnAddBorrow');
    const btnRetry = document.getElementById('btnRetry');

    // 新增按钮
    btnAdd?.addEventListener('click', () => openModalForAdd());

    // 重试按钮
    btnRetry?.addEventListener('click', () => loadBorrows());

    // 搜索过滤（数据加载后自动生效）
    initSearchFilter();

    // 表格内操作按钮（事件委托）
    initRowActions();

    // Modal 表单提交
    initModalForm();

    // 加载数据
    loadBorrows();
});


/* =========================================================
   1. 加载列表
   ========================================================= */
async function loadBorrows() {
    showState('loading');
    try {
        const res = await fetch(API_CONFIG.listUrl);
        if (!res.ok) throw new Error('HTTP ' + res.status);

        const json = await res.json();
        borrowsData = normalizeList(json);

        renderTable(borrowsData);
        showState(borrowsData.length === 0 ? 'empty' : 'table');
        updateCount(borrowsData.length);

        // 渲染完成后立即应用一次搜索词（防止重试后搜索状态丢失）
        applySearch();

    } catch (err) {
        // TODO(后端): 如果 /borrows/list 还没实现，会走到这里（HTTP 404）
        //             请先实现 BorrowListServlet 返回 JSON，参考文件末尾 TODO
        console.error('[加载借阅记录异常]', err);
        document.getElementById('errorMsg').textContent =
            '加载失败：' + (err.message || '未知错误');
        showState('error');
    }
}

/** 兼容后端返回 { data:[...] } / { list:[...] } / 直接数组 三种情况 */
function normalizeList(json) {
    if (Array.isArray(json)) return json;
    if (json && Array.isArray(json.data)) return json.data;
    if (json && Array.isArray(json.list)) return json.list;
    return [];
}

/** 在 loading / error / empty / table 四种状态间切换 */
function showState(state) {
    const map = {
        loading: 'loadingState',
        error:   'errorState',
        empty:   'emptyState',
        table:   'tableCard',
    };
    Object.values(map).forEach(id => {
        document.getElementById(id)?.classList.add('d-none');
    });
    if (map[state]) document.getElementById(map[state])?.classList.remove('d-none');

    // 表格显示时隐藏"无结果"，由 applySearch 重新判断
    if (state !== 'table') {
        document.getElementById('noResult')?.classList.add('d-none');
    }
}

/** 更新"共 X 条记录" */
function updateCount(n) {
    const el = document.getElementById('borrowCount');
    if (el) el.textContent = n;
}


/* =========================================================
   2. 渲染表格
   ========================================================= */
function renderTable(list) {
    const tbody = document.getElementById('borrowTbody');
    if (!tbody) return;

    if (!list.length) {
        tbody.innerHTML = '';
        return;
    }

    tbody.innerHTML = list.map(b => {
        const status = computeStatus(b);
        const badge  = statusBadge(status);
        // 已还的记录不再显示"归还"按钮
        const returnBtn = status === '已还'
            ? ''
            : `<button type="button" class="btn btn-sm btn-outline-success"
                       data-action="return" data-id="${esc(b.borrowId)}"
                       data-title="${esc(b.title)}">
                   <i class="bi bi-arrow-return-left"></i> 归还
               </button>`;

        return `
        <tr>
            <td class="text-secondary">${esc(b.borrowId)}</td>
            <td>
                <i class="bi bi-book text-primary me-2"></i>
                <span class="book-title">${esc(b.title)}</span>
            </td>
            <td>${esc(b.borrower)}</td>
            <td>${formatDate(b.borrowDate)}</td>
            <td>${formatDate(b.dueDate)}</td>
            <td>${formatDate(b.returnDate)}</td>
            <td>${badge}</td>
            <td class="text-end">
                <button type="button" class="btn btn-sm btn-outline-primary"
                        data-action="edit" data-id="${esc(b.borrowId)}">
                    <i class="bi bi-pencil"></i> 编辑
                </button>
                ${returnBtn}
                <button type="button" class="btn btn-sm btn-outline-danger"
                        data-action="delete" data-id="${esc(b.borrowId)}"
                        data-title="${esc(b.title)}">
                    <i class="bi bi-trash"></i> 删除
                </button>
            </td>
        </tr>`;
    }).join('');
}

/**
 * 计算状态
 * 1) 优先使用后端返回的 status（"在借" / "已还" / "逾期"）
 * 2) 否则前端兜底计算：
 *    - 有 returnDate       → 已还
 *    - dueDate < 今天      → 逾期
 *    - 其余                → 在借
 */
function computeStatus(b) {
    if (b.status) return b.status;
    if (b.returnDate) return '已还';
    if (b.dueDate && String(b.dueDate).slice(0, 10) < todayStr()) return '逾期';
    return '在借';
}

/** 状态 → Bootstrap 徽章 */
function statusBadge(status) {
    const cls = {
        '在借': 'bg-primary',
        '已还': 'bg-success',
        '逾期': 'bg-danger',
    }[status] || 'bg-secondary';
    return `<span class="badge ${cls}">${esc(status)}</span>`;
}


/* =========================================================
   3. 搜索过滤（纯前端）
   ========================================================= */
function initSearchFilter() {
    const input = document.getElementById('searchInput');
    if (!input) return;

    input.addEventListener('input', () => applySearch());
}

function applySearch() {
    const input = document.getElementById('searchInput');
    const tableCard = document.getElementById('tableCard');
    const noResult  = document.getElementById('noResult');
    const tbody     = document.getElementById('borrowTbody');

    if (!input || !tbody || tableCard?.classList.contains('d-none')) return;

    const keyword = input.value.trim().toLowerCase();
    const rows    = tbody.querySelectorAll('tr');
    let visible   = 0;

    rows.forEach(row => {
        // 第 2 列书名、第 3 列借阅人参与匹配
        const title    = row.children[1]?.innerText.toLowerCase() || '';
        const borrower = row.children[2]?.innerText.toLowerCase() || '';
        const match    = !keyword || title.includes(keyword) || borrower.includes(keyword);
        row.style.display = match ? '' : 'none';
        if (match) visible++;
    });

    const hasResult = visible > 0;
    noResult?.classList.toggle('d-none', hasResult);
    tableCard?.classList.toggle('d-none', !hasResult);
}


/* =========================================================
   4. 表格内操作（事件委托）
   ========================================================= */
function initRowActions() {
    const tbody = document.getElementById('borrowTbody');
    if (!tbody) return;

    tbody.addEventListener('click', e => {
        const btn = e.target.closest('button[data-action]');
        if (!btn) return;

        const action = btn.dataset.action;
        const id     = btn.dataset.id;
        const title  = btn.dataset.title || '';

        if (action === 'edit')   openModalForEdit(id);
        if (action === 'return') handleReturn(id, title);
        if (action === 'delete') handleDelete(id, title);
    });
}


/* =========================================================
   5. 新增 / 编辑 弹窗
   ========================================================= */
let modalInstance = null;

function getModal() {
    if (!modalInstance) {
        modalInstance = bootstrap.Modal.getOrCreateInstance(
            document.getElementById('borrowModal')
        );
    }
    return modalInstance;
}

/** 新增模式 */
function openModalForAdd() {
    const form = document.getElementById('borrowForm');
    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('fieldBorrowId').value = '';
    document.getElementById('borrowModalTitle').textContent = '新增借阅';

    // 默认日期：借出=今天，应还=今天+30 天
    document.getElementById('fieldBorrowDate').value = todayStr();
    document.getElementById('fieldDueDate').value    = addDays(todayStr(), 30);
    document.getElementById('fieldReturnDate').value = '';

    hideModalMsg();
    getModal().show();
}

/** 编辑模式 */
function openModalForEdit(id) {
    const item = borrowsData.find(b => String(b.borrowId) === String(id));
    if (!item) {
        alert('未找到该记录，请刷新页面后重试');
        return;
    }

    const form = document.getElementById('borrowForm');
    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('fieldBorrowId').value  = item.borrowId ?? '';
    document.getElementById('borrowModalTitle').textContent = '编辑借阅记录';

    document.getElementById('fieldTitle').value      = item.title      ?? '';
    document.getElementById('fieldBorrower').value   = item.borrower   ?? '';
    document.getElementById('fieldBorrowDate').value = toDateInput(item.borrowDate);
    document.getElementById('fieldDueDate').value    = toDateInput(item.dueDate);
    document.getElementById('fieldReturnDate').value = toDateInput(item.returnDate);

    hideModalMsg();
    getModal().show();
}

/** Modal 表单提交（新增 / 编辑 共用） */
function initModalForm() {
    const form = document.getElementById('borrowForm');
    if (!form) return;

    form.addEventListener('submit', async e => {
        e.preventDefault();

        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const btn = document.getElementById('btnSaveBorrow');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>保存中...';

        const payload = {
            title:      form.title.value.trim(),
            borrower:   form.borrower.value.trim(),
            borrowDate: form.borrowDate.value || '',
            dueDate:    form.dueDate.value    || '',
            returnDate: form.returnDate.value || '',
        };

        const id = document.getElementById('fieldBorrowId').value;
        const isEdit = !!id;
        if (isEdit) payload.borrowId = id;

        try {
            const res = await fetch(isEdit ? API_CONFIG.updateUrl : API_CONFIG.addUrl, {
                method: 'POST',
                body: new URLSearchParams(payload),
            });
            if (!res.ok) throw new Error('HTTP ' + res.status);

            const data = await res.json();
            if (!data.success) {
                showModalMsg(data.message || '保存失败', 'danger');
                resetSaveBtn(btn);
                return;
            }

            // 成功：关闭弹窗 + 刷新列表
            showModalMsg(isEdit ? '修改成功' : '新增成功', 'success');
            setTimeout(async () => {
                getModal().hide();
                resetSaveBtn(btn);
                await loadBorrows();
            }, 500);

        } catch (err) {
            // TODO(后端): 如果 /borrows/add 或 /borrows/update 还没实现，会走到这里
            console.error('[保存借阅异常]', err);
            showModalMsg('保存失败：' + err.message, 'danger');
            resetSaveBtn(btn);
        }
    });
}

function resetSaveBtn(btn) {
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-check-lg me-1"></i> 保存';
}

function showModalMsg(text, type) {
    const el = document.getElementById('modalMsg');
    if (!el) return;
    el.className = 'small mt-3 text-' + type;
    el.textContent = text;
    el.classList.remove('d-none');
}

function hideModalMsg() {
    const el = document.getElementById('modalMsg');
    if (!el) return;
    el.classList.add('d-none');
    el.textContent = '';
}


/* =========================================================
   6. 归还
   ========================================================= */
async function handleReturn(id, title) {
    if (!confirm(`确定归还《${title}》吗？`)) return;

    try {
        // TODO(前端): 如果 contextPath 不是根路径，把 'borrows/return' 改成 '/你的contextPath/borrows/return'
        const res = await fetch(
            API_CONFIG.returnUrl + '?id=' + encodeURIComponent(id),
            { method: 'POST' }
        );
        if (!res.ok) throw new Error('HTTP ' + res.status);

        const data = await res.json();
        if (!data.success) {
            alert(data.message || '归还失败');
            return;
        }

        // 归还成功：直接刷新列表（状态/归还日期都会变）
        await loadBorrows();

    } catch (err) {
        // TODO(后端): 参考文件末尾 TODO，需要实现 BorrowReturnServlet
        console.error('[归还异常]', err);
        alert('归还失败，请稍后再试');
    }
}


/* =========================================================
   7. 删除
   ========================================================= */
async function handleDelete(id, title) {
    if (!confirm(`确定要删除《${title}》的借阅记录吗？\n该操作不可恢复。`)) return;

    try {
        const res = await fetch(
            API_CONFIG.deleteUrl + '?id=' + encodeURIComponent(id),
            { method: 'POST' }
        );
        if (!res.ok) throw new Error('HTTP ' + res.status);

        const data = await res.json();
        if (!data.success) {
            alert(data.message || '删除失败');
            return;
        }

        // 删除成功：移除本地缓存中的对应项 + 重渲染
        borrowsData = borrowsData.filter(b => String(b.borrowId) !== String(id));
        renderTable(borrowsData);
        updateCount(borrowsData.length);

        if (borrowsData.length === 0) {
            showState('empty');
        } else {
            applySearch(); // 重新应用搜索词，保持过滤状态
        }

    } catch (err) {
        // TODO(后端): 参考文件末尾 TODO，需要实现 BorrowDeleteServlet
        console.error('[删除异常]', err);
        alert('删除失败，请稍后再试');
    }
}


/* =========================================================
   8. 工具函数
   ========================================================= */

/** HTML 转义，防止标题/借阅人里带特殊字符破坏 DOM */
function esc(s) {
    return String(s ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
    }[c]));
}

/** 后端日期（可能是 "2024-01-01" 或 "2024-01-01T00:00:00"）→ 只显示前 10 位 */
function formatDate(s) {
    if (!s) return '<span class="text-secondary">-</span>';
    return esc(String(s).slice(0, 10));
}

/** 把后端日期规范成 <input type="date"> 需要的 YYYY-MM-DD */
function toDateInput(s) {
    if (!s) return '';
    return String(s).slice(0, 10);
}

/** 今天的 YYYY-MM-DD */
function todayStr() {
    const d = new Date();
    const p = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

/** 在 YYYY-MM-DD 上加天数 */
function addDays(dateStr, days) {
    const d = new Date(dateStr + 'T00:00:00');
    d.setDate(d.getDate() + days);
    const p = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}


/* =========================================================
   ⚠️ TODO(后端)：本页需要 5 个接口，均为 JSON 返回
   =========================================================

   1) GET /borrows/list
      返回：{ "success": true, "data": [
          { "borrowId": 1, "title": "Java 编程思想", "borrower": "张三",
            "borrowDate": "2024-01-01", "dueDate": "2024-01-31",
            "returnDate": null, "status": "在借" }
      ]}
      说明：status 可以不返回，前端会按 returnDate / dueDate 自动兜底计算

   2) POST /borrows/add
      请求体：title, borrower, borrowDate, dueDate, returnDate(可空)
      返回：{ "success": true } / { "success": false, "message": "..." }

   3) POST /borrows/update
      请求体：borrowId, title, borrower, borrowDate, dueDate, returnDate(可空)
      返回：同 2)

   4) POST /borrows/return?id=1
      后端把该记录的 returnDate 设为今天，返回：{ "success": true }

   5) POST /borrows/delete?id=1
      返回：{ "success": true }

   =========================================================
   ⚠️ TODO(数据库)：需要在 borrow 表补充以下字段
   =========================================================
     borrow_date   DATE     借出日期
     due_date      DATE     应还日期
     return_date   DATE     归还日期（NULL 表示未归还）
     status        VARCHAR  状态（可选；不存则由前端计算）
   ========================================================= */