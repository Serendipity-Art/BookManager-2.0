package com.bookmanager.service;

import com.bookmanager.entity.Book;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/23
 */
public interface BookService {
    List<Book> showAllBook();//展示图书列表
    int addBook(Book book);//添加图书
    int deleteBook(@Param("bid") int bid);//删除图书
}
