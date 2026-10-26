package com.bookmanager.servlet;

import com.bookmanager.entity.User;
import com.bookmanager.service.Impl.UserServiceImpl;
import com.bookmanager.service.UserService;
import com.bookmanager.utils.ThymeleafUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
@WebServlet(value = "/login", loadOnStartup = 1)
public class LoginServlet extends HttpServlet {
    //类的私有化字段,仅供本类使用,其他类一律不能操作
    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private UserService service;
    private JakartaServletWebApplication thymeleafApp;
    @Override
    public void init() throws ServletException {
        //this寻找当前对象的实列,也就是上面定义的私有化字段,不加也可以
        this.service = new UserServiceImpl();
        this.thymeleafApp = JakartaServletWebApplication
                .buildApplication(getServletContext());
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        //1.设置响应格式字符
        resp.setContentType("text/html;charset=UTF-8");
        // 2. 为 Thymeleaf 构建 Web 环境交换对象
        var webExchange = thymeleafApp.buildExchange(req, resp);
        // 3. 创建 WebContext，而不是原来的 Context
        WebContext context = new WebContext(webExchange);
        // 4. 调用工具类
        try {
            ThymeleafUtil.process("login", context, resp.getWriter());
        } catch (Exception e) {
            log.error("页面渲染失败", e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "页面渲染失败");
            }
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return;
            }
            User user = service.auth(username, password);
            if (user != null) {
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("登录异常", e);
            result.put("success", false);
            result.put("message", "系统繁忙,请稍后再尝试");
        }
        try {
            MAPPER.writeValue(resp.getWriter(), result);
        } catch (Exception e) {
            log.error("响应的JSON序列失败");
            if(!resp.isCommitted()){
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
