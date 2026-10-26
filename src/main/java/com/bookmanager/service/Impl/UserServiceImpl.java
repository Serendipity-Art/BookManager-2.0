package com.bookmanager.service.Impl;

import com.bookmanager.entity.User;
import com.bookmanager.mapper.UserMapper;
import com.bookmanager.service.UserService;
import com.bookmanager.utils.MybatisUtil;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.session.SqlSession;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
public class UserServiceImpl implements UserService {
    Log log;
    @Override
    public User auth(String username, String password) {
        // 1. 空值校验，避免拼出 WHERE username = null 这种怪 SQL
        if (username == null || password == null
                || username.isBlank() || password.isBlank()) {
            return null;
        }
        try (SqlSession sqlSession= MybatisUtil.getSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = userMapper.getUser(username, password);
            System.out.println(">>> SQL 查询结果: " + user);   // ★ 关键
            return user;
        } catch (Exception e) {
            log.error("登录失败, username=" + username, e);
            return null;
        }
    }
}
