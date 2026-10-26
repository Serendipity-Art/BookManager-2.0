/* ================================================================
   ★★★ 后端联调重点区域（一）：接口配置 ★★★
   只需要改这里的两个值，就能对接你的后端
   ================================================================ */
const API_CONFIG = {
    loginUrl:    'login',       // TODO: 改成你的后端登录接口地址
    redirectUrl: 'dashboard',   // TODO: 改成登录成功后跳转的页面
};

/* ================================================================
   ★★★ 后端联调重点区域（二）：登录请求函数 ★★★
   返回约定：{ success: true, token: 'xxx', redirect: 'xxx' }
           或 { success: false, message: '错误提示' }
   ================================================================ */
async function loginRequest(username, password) {

    // ---------- 【真实后端联调：取消下面这段注释，并删掉最下面的"演示模拟"】 ----------
    const response = await fetch(API_CONFIG.loginUrl, {
      method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   // TODO: 字段名按你后端的要求改，比如后端要 account / pwd
      body: new URLSearchParams({ username, password }),
    });
    //
    if (!response.ok) {
      throw new Error('HTTP ' + response.status);
    }
    //
    const data = await response.json();
    //
    // // TODO: 按你后端实际返回的结构改，下面假设返回 { code: 0, message, token }
    if (!data.success) {
      return { success: false, message: data.message || '登录失败' };
    }
    return {
      success: true, // TODO: 换成你后端返回的 token 字段
      redirect: API_CONFIG.redirectUrl,
    };
    // ------------------------------------------------------------------------------

    // ---------- 【演示模拟：联调时删掉这一段】 ----------
    // await new Promise(resolve => setTimeout(resolve, 800));
    // if (username === 'admin' && password === '123456') {
    //     return { success: true, token: 'demo-token-please-replace', redirect: API_CONFIG.redirectUrl };
    // }
    // return { success: false, message: '用户名或密码错误（演示账号：admin / 123456）' };
    // ------------------------------------------------------------------------------
}

/* ================================================================
   以下为页面交互逻辑，通常不需要改动
   ================================================================ */
document.addEventListener('DOMContentLoaded', () => {

    const form       = document.getElementById('loginForm');
    const alertBox   = document.getElementById('loginAlert');
    const submitBtn  = document.getElementById('submitBtn');
    const btnSpinner = document.getElementById('btnSpinner');
    const btnText    = document.getElementById('btnText');
    const usernameEl = document.getElementById('username');
    const passwordEl = document.getElementById('password');
    const rememberEl = document.getElementById('rememberMe');

    // 页脚年份自动更新
    document.getElementById('year').textContent = new Date().getFullYear();

    // 页面加载时：如果上次勾选了"记住我"，回填用户名（只回填用户名，不存密码）
    const remembered = localStorage.getItem('rememberedUsername');
    if (remembered) {
        usernameEl.value = remembered;
        rememberEl.checked = true;
    }

    // 密码显示 / 隐藏切换
    document.getElementById('togglePassword').addEventListener('click', () => {
        const icon = document.getElementById('togglePasswordIcon');
        const isHidden = passwordEl.type === 'password';
        passwordEl.type = isHidden ? 'text' : 'password';
        icon.className = isHidden ? 'bi bi-eye-slash' : 'bi bi-eye';
    });

    // 统一控制按钮加载状态
    function setLoading(loading) {
        submitBtn.disabled = loading;
        btnSpinner.classList.toggle('d-none', !loading);
        btnText.textContent = loading ? '登录中...' : '登 录';
    }

    function showAlert(message) {
        alertBox.textContent = message;
        alertBox.classList.remove('d-none');
    }

    function hideAlert() {
        alertBox.classList.add('d-none');
        alertBox.textContent = '';
    }

    // 表单提交
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideAlert();

        // 前端基础校验（必填项）
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const username   = usernameEl.value.trim();
        const password   = passwordEl.value;
        const rememberMe = rememberEl.checked;

        setLoading(true);
        try {
            const result = await loginRequest(username, password, rememberMe);

            if (!result.success) {
                showAlert(result.message || '登录失败，请重试');
                return;
            }

            /* ================================================================
               ★★★ 后端联调重点区域（三）：登录成功后的存储与跳转 ★★★
               ================================================================ */
            // TODO: 如果你的后端用 cookie/session，这里可能不需要存 token
            //       如果返回的字段名不是 token，请同步修改
            localStorage.setItem('authToken', result.token);

            // "记住我"只记住用户名，绝不存密码
            if (rememberMe) {
                localStorage.setItem('rememberedUsername', username);
            } else {
                localStorage.removeItem('rememberedUsername');
            }

            // TODO: 根据后端返回的 redirect 跳转，字段名不一致就改这里
            window.location.href = result.redirect || API_CONFIG.redirectUrl;

        } catch (err) {
            console.error('[登录请求异常]', err);
            showAlert('无法连接服务器，请稍后再试');
        } finally {
            setLoading(false);
        }
    });

});