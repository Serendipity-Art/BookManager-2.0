package com.bookmanager.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
public class MybatisUtil {
    private static SqlSessionFactory factory;
    static{
        try {
            factory=new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static SqlSession getSession(){
        return factory.openSession(true);
    }
}
