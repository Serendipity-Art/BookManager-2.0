package com.bookmanager.servlet;

import com.bookmanager.entity.Borrow;
import com.bookmanager.service.BorrowService;
import com.bookmanager.service.Impl.BorrowServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 借阅记录 写操作接口（全部返回 JSON）
 * <p>
 * POST /borrows/add             表单: title, borrower, borrowDate, dueDate, returnDate(可空)
 * POST /borrows/update          表单: borrowId + 同上
 * POST /borrows/return?id=1     把 return_date 设成今天，status 置「已还」
 * POST /borrows/delete?id=1     删除
 * <p>
 * 统一返回：{"success":true} 或 {"success":false,"message":"给用户看的原因"}
 * <p>
 * 业务规则（书名→bid、姓名→sid、状态计算）都在 BorrowServiceImpl 里，
 * 这个类只负责：取参数 → 调 service → 把结果写成 JSON。
 *
 * @author Serendipity
 * @date 2026/10/25
 */
@WebServlet({"/borrows/add", "/borrows/update", "/borrows/return", "/borrows/delete"})
public class BorrowApiServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BorrowApiServlet.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BorrowService borrowService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.borrowService = new BorrowServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 中文书名/姓名必须靠这一行，否则按 ISO-8859-1 解码成乱码
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new LinkedHashMap<>();
        String path = req.getServletPath();

        try {
            switch (path) {
                case "/borrows/add"    -> borrowService.addBorrow(readForm(req));
                case "/borrows/update" -> borrowService.updateBorrow(readForm(req));
                case "/borrows/return" -> borrowService.returnBorrow(readId(req));
                case "/borrows/delete" -> borrowService.deleteBorrow(readId(req));
                default -> throw new IllegalArgumentException("未知接口：" + path);
            }
            result.put("success", true);
        } catch (IllegalArgumentException e) {
            // 业务校验失败（书名不存在、少了 id 之类）—— 是给用户看的话，不是系统故障
            result.put("success", false);
            result.put("message", e.getMessage() == null ? "参数不合法" : e.getMessage());
        } catch (Exception e) {
            log.error("借阅接口 {} 处理失败", path, e);
            result.put("success", false);
            result.put("message", "服务器内部错误，请稍后再试");
        }
        MAPPER.writeValue(resp.getWriter(), result);
        resp.getWriter().flush();
    }

    /* ==================== 参数解析 ==================== */

    private Borrow readForm(HttpServletRequest req) {
        Borrow borrow = new Borrow();
        borrow.setTitle(trim(req.getParameter("title")));
        borrow.setBorrower(trim(req.getParameter("borrower")));
        borrow.setBorrowdate(parseDate(req.getParameter("borrowDate"), "借出日期"));
        borrow.setDuedate(parseDate(req.getParameter("dueDate"), "应还日期"));
        borrow.setReturndate(parseDate(req.getParameter("returnDate"), "归还日期"));

        String idText = trim(req.getParameter("borrowId"));
        if (idText != null && !idText.isEmpty()) {
            borrow.setId(parseInt(idText, "借阅记录编号"));
        }
        return borrow;
    }

    private Integer readId(HttpServletRequest req) {
        String idText = trim(req.getParameter("id"));
        if (idText == null || idText.isEmpty()) {
            throw new IllegalArgumentException("缺少参数 id");
        }
        return parseInt(idText, "借阅记录编号");
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** <input type="date"> 传上来的就是 yyyy-MM-dd */
    private LocalDate parseDate(String text, String fieldName) {
        String s = trim(text);
        if (s == null) {
            return null;
        }
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + "格式不正确，应为 yyyy-MM-dd");
        }
    }

    private Integer parseInt(String text, String fieldName) {
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "必须是数字");
        }
    }
}
