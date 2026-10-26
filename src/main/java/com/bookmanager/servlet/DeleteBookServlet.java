package com.bookmanager.servlet;

import com.bookmanager.service.BookService;
import com.bookmanager.service.Impl.BookServiceImpl;
import com.bookmanager.service.UserService;
import com.bookmanager.utils.ThymeleafUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/23
 */
@WebServlet("/books/delete")
public class DeleteBookServlet extends HttpServlet {
    private BookService bookService;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    // ① 字段缓存,整个 Servlet 生命周期共用
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        super.init();  // 保留,HttpServlet#init() 是空实现,但养成习惯
        // ② 只初始化一次
        this.application = JakartaServletWebApplication
                .buildApplication(getServletContext());
        this.bookService = new BookServiceImpl();
    }

    //渲染页面
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        var webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange);
        ThymeleafUtil.process("books", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String id = req.getParameter("id");
            if (id == null || id.isBlank()) {
                result.put("success", false);
                result.put("message", "缺少id参数");
            } else {
                int bookId = Integer.parseInt(id);
                boolean deleted = bookService.deleteBook(bookId) > 0;
                result.put("success", true);
                result.put("message", deleted ? null : "图书不存在或已经删除");

            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "id格式错误");
        } catch (Exception e) {
            log("删除图书失败", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "服务器内部错误");
        }
        try {
            MAPPER.writeValue(resp.getWriter(), result);
        } catch (IOException e) {
            log("响应的 JSON 序列失败");
        }
    }
}
