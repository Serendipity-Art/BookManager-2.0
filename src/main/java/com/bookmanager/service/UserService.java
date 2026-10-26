package com.bookmanager.service;

import com.bookmanager.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
public interface UserService {
    User auth(String username, String password);
}
