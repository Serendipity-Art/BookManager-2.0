package com.bookmanager.servlet;

import com.bookmanager.entity.Book;
import com.bookmanager.service.BookService;
import com.bookmanager.service.Impl.BookServiceImpl;
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
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/23
 */
@WebServlet("/books")
public class BookListServlet extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(BookListServlet.class.getName());
    private JakartaServletWebApplication application;
    private BookService bookService;

    @Override
    public void init() throws ServletException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.bookService = new BookServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        List<Book> books;
        try {
            books = bookService.showAllBook();
            if (books == null || books.size() == 0) {
                books = Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("查询图书失败", e);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "查询图书失败");
            return;
        }
        var webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange);
        context.setVariable("books", books);
        context.setVariable("user", req.getSession().getAttribute("user"));
        try {
            StringWriter buffer = new StringWriter();
            ThymeleafUtil.process("books", context, buffer);
            resp.getWriter().write(buffer.toString());
        } catch (Exception e) {
            log.error("图书列表页渲染失败", e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "页面渲染失败");
            }
        }

    }
}
