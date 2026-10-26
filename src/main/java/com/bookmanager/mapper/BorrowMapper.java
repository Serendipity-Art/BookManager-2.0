package com.bookmanager.mapper;

import com.bookmanager.entity.Borrow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 借阅记录 数据访问层
 * <p>
 * 数据库真实字段（已核对）：
 * borrow(id, sid, bid, borrow_date, due_date, return_date, status)
 * book(bid, title, ...)   student(sid, name, ...)
 * <p>
 * 注意：实体属性名是 borrowdate / duedate / returndate / borrowstatus（没有下划线），
 * 所以这里统一给每列起别名，别名 == 实体属性名，MyBatis 才能自动映射。
 *
 * @author Serendipity
 * @date 2026/10/25
 */
public interface BorrowMapper {

    /* ==================== 列表：联表拿到书名和借阅人姓名 ==================== */
    @Select("""
            SELECT
                b.id          AS id,
                b.sid         AS sid,
                b.bid         AS bid,
                b.borrow_date AS borrowdate,
                b.due_date    AS duedate,
                b.return_date AS returndate,
                b.status      AS borrowstatus,
                bk.title      AS title,
                s.name        AS borrower
            FROM borrow b
            LEFT JOIN book    bk ON b.bid = bk.bid
            LEFT JOIN student s  ON b.sid = s.sid
            ORDER BY b.id DESC
            """)
    List<Borrow> getborrow();

    /* ==================== 前端只给书名/姓名，这里翻译成主键 ==================== */
    @Select("SELECT bid FROM book WHERE title = #{title} ORDER BY bid LIMIT 1")
    Integer findBidByTitle(@Param("title") String title);

    @Select("SELECT sid FROM student WHERE name = #{name} ORDER BY sid LIMIT 1")
    Integer findSidByName(@Param("name") String name);

    /* ==================== 新增 ==================== */
    @Insert("""
            INSERT INTO borrow (sid, bid, borrow_date, due_date, return_date, status)
            VALUES (#{sid}, #{bid}, #{borrowdate}, #{duedate}, #{returndate}, #{borrowstatus})
            """)
    int insertBorrow(Borrow borrow);

    /* ==================== 修改（借阅人 / 图书 / 日期都能改） ==================== */
    @Update("""
            UPDATE borrow
               SET sid         = #{sid},
                   bid         = #{bid},
                   borrow_date = #{borrowdate},
                   due_date    = #{duedate},
                   return_date = #{returndate},
                   status      = #{borrowstatus}
             WHERE id = #{id}
            """)
    int updateBorrow(Borrow borrow);

    /* ==================== 归还：只动 return_date 和 status ==================== */
    @Update("UPDATE borrow SET return_date = #{returnDate}, status = '已还' WHERE id = #{id}")
    int markReturned(@Param("id") Integer id, @Param("returnDate") LocalDate returnDate);

    /* ==================== 删除 ==================== */
    @Delete("DELETE FROM borrow WHERE id = #{id}")
    int deleteBorrow(@Param("id") Integer id);
}
