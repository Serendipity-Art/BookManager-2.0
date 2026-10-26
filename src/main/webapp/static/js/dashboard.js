document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('year').textContent = new Date().getFullYear();

    const today = new Date();
    document.getElementById('todayDate').textContent =
        today.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' });

    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('sidebarToggle');
    let backdrop = null;

    function closeSidebar() {
        sidebar.classList.remove('show');
        if (backdrop) { backdrop.remove(); backdrop = null; }
    }

    toggleBtn?.addEventListener('click', () => {
        const isOpen = sidebar.classList.toggle('show');
        if (isOpen) {
            backdrop = document.createElement('div');
            backdrop.className = 'sidebar-backdrop';
            backdrop.addEventListener('click', closeSidebar);
            document.body.appendChild(backdrop);
        } else {
            closeSidebar();
        }
    });
});