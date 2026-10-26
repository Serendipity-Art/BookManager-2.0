/* =========================================================
   图书管理页专属脚本
   公共逻辑（侧边栏折叠、#year）在 dashboard.js 中，这里不重复
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {

    /* =====================================================
       1. 搜索过滤（纯前端，已实现，无需后端）
       数据量小（<1000）时够用；数据量大改成后端分页 + 搜索接口
       ===================================================== */
    initSearchFilter();

    /* =====================================================
       2. 删除按钮（⚠️ 需要后端接口配合，见下方 TODO(后端)）
       ===================================================== */
    initDeleteButtons();

    /* =====================================================
       3. 新增 / 编辑按钮（⚠️ 需要后端页面配合，见下方 TODO(后端)）
       ===================================================== */
    // 当前是 <a href> 直接跳转，不需要 JS 拦截；
    // 如果将来改成弹窗 / AJAX 提交，再在这里加逻辑。
});


/* =========================================================
   1. 搜索过滤
   ========================================================= */
function initSearchFilter() {
    const searchInput = document.getElementById('searchInput');
    const bookTable   = document.getElementById('bookTable');
    const noResult    = document.getElementById('noResult');
    const tableCard   = document.getElementById('tableCard');

    if (!searchInput || !bookTable) return;   // 空列表时表格不存在，直接跳过

    searchInput.addEventListener('input', function () {
        const keyword = this.value.trim().toLowerCase();
        const rows    = bookTable.querySelectorAll('tbody tr');
        let visible   = 0;

        rows.forEach(function (row) {
            // 第 2 列书名 + 第 3 列作者参与匹配
            const title  = row.children[1].innerText.toLowerCase();
            const author = row.children[2].innerText.toLowerCase();
            const match  = !keyword || title.includes(keyword) || author.includes(keyword);

            row.style.display = match ? '' : 'none';
            if (match) visible++;
        });

        // 全部被过滤掉时切换显示
        const hasResult = visible > 0;
        if (noResult)  noResult.classList.toggle('d-none', hasResult);
        if (tableCard) tableCard.classList.toggle('d-none', !hasResult);
    });
}


/* =========================================================
   2. 删除
   ========================================================= */
function initDeleteButtons() {
    document.querySelectorAll('.btn-delete').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const id    = this.dataset.id;
            const title = this.dataset.title;

            if (!confirm(`确定要删除《${title}》吗？`)) return;

            deleteBook(id, this);
        });
    });
}

/**
 * 删除一本图书
 * @param {string|number} id     图书 id
 * @param {HTMLElement}   btnEl  触发删除的按钮（用于删行、更新计数）
 *
 * ============================================================
 * ⚠️ TODO(后端)：需要实现以下 Servlet，否则点删除会 404
 * ------------------------------------------------------------
 * @WebServlet("/books/delete")
 * public class BookDeleteServlet extends HttpServlet {
 *     @Override
 *     protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 *             throws ServletException, IOException {
 *         resp.setContentType("application/json;charset=UTF-8");
 *         String idStr = req.getParameter("id");
 *         try {
 *             Long id = Long.parseLong(idStr);
 *             // TODO: bookService.deleteBook(id);
 *             // 注意：外键约束时可能抛异常，需要 try-catch 返回 success:false
 *             resp.getWriter().write("{\"success\":true}");
 *         } catch (Exception e) {
 *             resp.getWriter().write("{\"success\":false,\"message\":\"删除失败\"}");
 *         }
 *     }
 * }
 * ============================================================
 */
async function deleteBook(id, btnEl) {
    try {
        // TODO(前端): 如果 contextPath 不是根路径，把 'books/delete' 改成 '/你的contextPath/books/delete'
        const res = await fetch('books/delete?id=' + encodeURIComponent(id), {
            method: 'POST'
        });

        if (!res.ok) throw new Error('HTTP ' + res.status);

        const data = await res.json();

        if (!data.success) {
            alert(data.message || '删除失败');
            return;
        }

        // ---- 删除成功：移除行 + 更新计数 ----
        const row = btnEl.closest('tr');
        if (row) row.remove();

        updateBookCount();

        // 删空了就刷新，让"空状态"显示出来
        const remaining = document.querySelectorAll('#bookTable tbody tr').length;
        if (remaining === 0) {
            location.reload();
        }

    } catch (err) {
        // 这里会捕获：404（后端没写）、500、JSON 解析失败、断网
        // ⚠️ 如果你的后端删除接口还没实现，就会走到这里 —— 看到"删除失败，请稍后再试"
        console.error('[删除异常]', err);
        alert('删除失败，请稍后再试');
    }
}

/** 更新页面上"共 X 本图书"的数字 */
function updateBookCount() {
    const counter = document.getElementById('bookCount');
    if (!counter) return;
    const rows = document.querySelectorAll('#bookTable tbody tr').length;
    counter.textContent = rows;
}


/* =========================================================
   3. 后续可扩展的 TODO（按需实现，当前不需要写）
   =========================================================
 *
 * TODO(前端): 分页
 *   - 后端 BookListServlet 增加 page / size 参数
 *   - 前端表格下方加 <nav><ul class="pagination">...
 *   - 切换页时 fetch(`books?page=${n}`) 再渲染
 *
 * TODO(前端): 排序
 *   - 表头 <th> 加点击事件
 *   - 后端 SQL 加 ORDER BY 参数
 *
 * TODO(前端): 批量删除
 *   - 表格首列加 checkbox
 *   - 顶部加"批量删除"按钮
 *   - 后端 /books/deleteBatch 接收 ids 数组
 *
 * TODO(前端): 新增/编辑改成弹窗（不跳页）
 *   - Bootstrap Modal + 表单
 *   - POST 到 /books/save，成功后就地刷新表格
 */