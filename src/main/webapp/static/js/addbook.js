/* =========================================================
   新增图书页专用脚本
   ========================================================= */

const API_CONFIG = {
    addBookUrl: 'new',   // 和 @WebServlet("/books/new") 对应
    listUrl:    '../books',
};

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('bookForm');
    if (!form) return;

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const btn = document.getElementById('btnSubmit');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>保存中...';

        try {
            const res = await addBookRequest(
                form.title.value.trim(),
                form.author.value.trim(),
                form.price.value.trim(),
                form.description.value.trim()
            );

            if (res.success) {
                showMsg('添加成功，正在返回列表...', 'success');
                setTimeout(() => { window.location.href = API_CONFIG.listUrl; }, 600);
            } else {
                showMsg(res.message, 'danger');
                resetBtn(btn);
            }
        } catch (err) {
            console.error('[添加异常]', err);
            showMsg('请求失败：' + err.message, 'danger');
            resetBtn(btn);
        }
    });
});
//saync是关键字,表明这是一个异步函数,只有添加异步,函数内部才能用await
async function addBookRequest(title, author, price, description) {
    const res = await fetch(API_CONFIG.addBookUrl, {
        //前端发送post请求,请求体里面包括了书籍信息的四个参数,Get:"读取数据",Post:"提交数据"
        method: 'POST',
        // URLSearchParams 自动设置
        // Content-Type: application/x-www-form-urlencoded;charset=UTF-8
        body: new URLSearchParams({ title, author, price, description }),
    });
    //res.ok,布尔值,判断状态码200~299:true,否则为false
    if (!res.ok) throw new Error('HTTP ' + res.status);

    const data = await res.json();
    if (!data.success) {
        //短路求值,左边有值用左边,否则用右边
        return { success: false, message: data.message || '添加失败' };
    }
    return { success: true, message: data.message || '添加成功', bid: data.bid };
}

function resetBtn(btn) {
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-check-lg me-1"></i> 保存';
}

function showMsg(text, type) {
    const el = document.getElementById('formMsg');
    if (!el) return;
    el.className = 'small mt-3 text-' + type;
    el.textContent = text;
    el.classList.remove('d-none');
}