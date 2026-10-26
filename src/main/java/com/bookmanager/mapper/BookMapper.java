package com.bookmanager.mapper;

import com.bookmanager.entity.Book;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/23
 */
public interface BookMapper {
    @Select("select * from book")
    List<Book> getBooks();
    @Insert("insert into book (bid, title, author, price, description) values (#{bid},#{title},#{author},#{price},#{description})")
    int addBook(Book book);
    @Delete("delete from book where bid=#{bid}")
    int deleteBook(@Param("bid") int bid);
}
