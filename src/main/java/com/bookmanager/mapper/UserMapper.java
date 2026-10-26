package com.bookmanager.mapper;

import com.bookmanager.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
public interface UserMapper {
    @Select("select * from admin where username=#{username} and password=#{password}")
    User getUser(@Param("username") String username, @Param("password") String password);
}
