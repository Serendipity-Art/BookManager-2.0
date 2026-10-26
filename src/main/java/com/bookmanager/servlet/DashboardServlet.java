package com.bookmanager.servlet;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
import com.bookmanager.utils.ThymeleafUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private JakartaServletWebApplication application;
    private static final Logger log = LoggerFactory.getLogger(DashboardServlet.class);

    @Override
    public void init() throws ServletException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        var webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange);
        // 把 session 里的 user 传给模板，顶栏显示用户名
        context.setVariable("user", req.getSession().getAttribute("user"));
        try {
            ThymeleafUtil.process("dashboard", context, resp.getWriter());
        } catch (Exception e) {
            log.error("渲染失败",e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "页面渲染失败");
            }
        }
    }
}
