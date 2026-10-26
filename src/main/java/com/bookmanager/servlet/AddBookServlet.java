package com.bookmanager.servlet;

import com.bookmanager.entity.Book;
import com.bookmanager.service.BookService;
import com.bookmanager.service.Impl.BookServiceImpl;
import com.bookmanager.utils.ThymeleafUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/23
 */
@WebServlet("/books/new")
public class AddBookServlet extends HttpServlet {
    private final BookService bookService = new BookServiceImpl();
    private static final Logger log = LoggerFactory.getLogger(AddBookServlet.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    //渲染页面
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        var webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(req, resp);
        WebContext context = new WebContext(webExchange);
        ThymeleafUtil.process("addbook", context, resp.getWriter());
    }
    //接受前端传递的参数
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<>();
        Book book = new Book();
        //1.参数校验
        try {
            String title = req.getParameter("title");
            String author = req.getParameter("author");
            String priceStr = req.getParameter("price");
            String description = req.getParameter("description");
            if (title == null || title.isBlank() || author == null || author.isBlank() || priceStr == null || priceStr.isBlank() || description == null || description.isBlank()) {
                result.put("success", false);
                result.put("message", "缺少相关书籍信息");
                return;
            }
            //2.价格格式转换
            int price;
            try {
                price = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "价格格式不正确");
                writeJson(resp, result);
                return;
            }
            //3.入库
            book.title(title).author(author).price(price).description(description);
            int rows = bookService.addBook(book);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "添加成功");
                result.put("bid", book.bid());
            } else {
                result.put("success", false);
                result.put("message", "添加书籍失败");
            }
        } catch (Exception e) {
            log.error("添加书籍失败, book={}", book, e);
            result.clear();
            result.put("success", false);
            result.put("message", "服务器内部错误");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        writeJson(resp, result);
    }
        private void writeJson (HttpServletResponse resp, Map < String, Object > result){
            try {
                mapper.writer().writeValue(resp.getWriter(), result);
                resp.getWriter().flush();
            } catch (IOException e) {
                log.error("响应的JSON序列失败");
            }
        }
}