package com.bookmanager.filter;

import com.bookmanager.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URL;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
@WebFilter("/*")
public class MainFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String url = req.getRequestURI().toString();
        if (!url.contains("/static/")&&!url.endsWith("login")) {
            HttpSession session = req.getSession();
            User user = (User) session.getAttribute("user");
            if (user == null) {
                res.sendRedirect(req.getContextPath() + "/login");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
