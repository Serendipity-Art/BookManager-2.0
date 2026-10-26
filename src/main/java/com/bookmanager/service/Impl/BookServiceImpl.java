package com.bookmanager.service.Impl;

import com.bookmanager.entity.Book;
import com.bookmanager.entity.User;
import com.bookmanager.mapper.BookMapper;
import com.bookmanager.mapper.UserMapper;
import com.bookmanager.service.BookService;
import com.bookmanager.utils.MybatisUtil;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/23
 */
public class BookServiceImpl implements BookService {
    public List<Book> showAllBook() {
        try (SqlSession sqlSession = MybatisUtil.getSession()) {
            BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
            List<Book> books = bookMapper.getBooks();
            System.out.println("SQL 图书查询结果: "+books);
            return books;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }
    public int addBook(Book book) {
        try (SqlSession sqlSession = MybatisUtil.getSession()) {
            BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
            int result=bookMapper.addBook(book);
            System.out.println(result>0?"插入成功":"插入失败,请检查输入的信息是否完整");
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    public int deleteBook(@Param("bid") int bid) {
        try (SqlSession sqlSession = MybatisUtil.getSession()) {
            BookMapper bookMapper = sqlSession.getMapper(BookMapper.class);
            int result=bookMapper.deleteBook(bid);
            System.out.println(result > 0 ? "删除成功" : "删除失败");
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}
