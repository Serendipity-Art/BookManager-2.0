package com.bookmanager.servlet;

import com.bookmanager.entity.Borrow;
import com.bookmanager.service.BorrowService;
import com.bookmanager.service.Impl.BorrowServiceImpl;
import com.bookmanager.utils.ThymeleafUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅记录：一个 URL 只干一件事
 * <p>
 * GET /borrows      → 渲染 borrows.html（服务端出页面）
 * GET /borrows/list → 返回纯 JSON 给前端 fetch
 * <p>
 * JSON 契约（必须与 static/js/borrows.js 保持一致）：
 * {"success":true,"data":[
 *   {"borrowId":1,"title":"Java核心技术 卷I","borrower":"朱俊衡",
 *    "borrowDate":"2026-08-05","dueDate":"2026-09-04",
 *    "returnDate":"2026-08-28","status":"已还"}
 * ]}
 *
 * @author Serendipity
 * @date 2026/10/24
 */
@WebServlet({"/borrows", "/borrows/list"})
public class BorrowListServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BorrowListServlet.class);

    /** LocalDate 必须注册 JavaTimeModule，否则 Jackson 直接报错 */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JakartaServletWebApplication thymeleafApp;
    private BorrowService borrowService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.thymeleafApp = JakartaServletWebApplication.buildApplication(getServletContext());
        this.borrowService = new BorrowServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/borrows".equals(req.getServletPath())) {
            renderPage(req, resp);
        } else {
            writeListJson(resp);
        }
    }

    /* ==================== 1. HTML 页面 ==================== */
    private void renderPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");

        List<Borrow> borrows;
        try {
            borrows = borrowService.showAllBorrow();
        } catch (Exception e) {
            log.error("查询借阅记录失败", e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "查询借阅记录失败");
            }
            return;
        }

        WebContext context = new WebContext(thymeleafApp.buildExchange(req, resp));
        context.setVariable("borrows", borrows);
        context.setVariable("user", req.getSession().getAttribute("user"));

        // 先渲染进缓冲区：模板中途报错时响应还没提交，还能正常返回 500 而不是吐出半页 HTML
        try {
            StringWriter buffer = new StringWriter();
            ThymeleafUtil.process("borrows", context, buffer);
            resp.getWriter().write(buffer.toString());
        } catch (Exception e) {
            log.error("借阅记录页渲染失败", e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "页面渲染失败");
            }
        }
    }

    /* ==================== 2. JSON 接口 ==================== */
    private void writeListJson(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<Borrow> borrows = borrowService.showAllBorrow();
            result.put("success", true);
            result.put("total", borrows.size());
            result.put("data", borrows);
        } catch (Exception e) {
            log.error("获取借阅列表失败", e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取借阅信息失败");
            }
            return;
        }
        // 只序列化一次。写成 writeValue(writer, 已经是 String 的 json) 会二次转义成 "{\"success\":true,...}"
        MAPPER.writeValue(resp.getWriter(), result);
        resp.getWriter().flush();
    }
}
