package com.bookmanager.service.Impl;

import com.bookmanager.entity.Borrow;
import com.bookmanager.mapper.BorrowMapper;
import com.bookmanager.service.BorrowService;
import com.bookmanager.utils.MybatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.time.LocalDate;
import java.util.List;

/**
 * 借阅记录 业务逻辑实现
 * <p>
 * 两条纪律（之前踩过的坑）：
 * 1. 不许 catch 之后再 return 空集合 —— 异常要往上抛，否则前端只会看到"共 0 条"，
 * 真正的 SQL 错误被吞掉，排查时会疯。
 * 2. sid / bid 由本层解析，Servlet 层只负责搬参数。
 *
 * @author Serendipity
 * @date 2026/10/25
 */
public class BorrowServiceImpl implements BorrowService {

    private static final String STATUS_BORROWED = "在借";
    private static final String STATUS_OVERDUE  = "逾期";
    private static final String STATUS_RETURNED = "已还";

    @Override
    public List<Borrow> showAllBorrow() {
        try (SqlSession session = MybatisUtil.getSession()) {
            return session.getMapper(BorrowMapper.class).getborrow();
        }
    }

    @Override
    public void addBorrow(Borrow borrow) {
        try (SqlSession session = MybatisUtil.getSession()) {
            BorrowMapper mapper = session.getMapper(BorrowMapper.class);
            resolveKeys(borrow, mapper);              // 书名 → bid，姓名 → sid
            if (borrow.getBorrowdate() == null) {
                borrow.setBorrowdate(LocalDate.now());
            }
            borrow.setBorrowstatus(computeStatus(borrow));
            mapper.insertBorrow(borrow);
        }
    }

    @Override
    public void updateBorrow(Borrow borrow) {
        if (borrow.getId() == null) {
            throw new IllegalArgumentException("缺少借阅记录编号，无法修改");
        }
        try (SqlSession session = MybatisUtil.getSession()) {
            BorrowMapper mapper = session.getMapper(BorrowMapper.class);
            resolveKeys(borrow, mapper);
            borrow.setBorrowstatus(computeStatus(borrow));
            if (mapper.updateBorrow(borrow) == 0) {
                throw new IllegalArgumentException("这条借阅记录已不存在，请刷新页面后重试");
            }
        }
    }

    @Override
    public void returnBorrow(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("缺少借阅记录编号");
        }
        try (SqlSession session = MybatisUtil.getSession()) {
            BorrowMapper mapper = session.getMapper(BorrowMapper.class);
            if (mapper.markReturned(id, LocalDate.now()) == 0) {
                throw new IllegalArgumentException("这条借阅记录已不存在，请刷新页面后重试");
            }
        }
    }

    @Override
    public void deleteBorrow(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("缺少借阅记录编号");
        }
        try (SqlSession session = MybatisUtil.getSession()) {
            BorrowMapper mapper = session.getMapper(BorrowMapper.class);
            if (mapper.deleteBorrow(id) == 0) {
                throw new IllegalArgumentException("这条借阅记录已不存在，请刷新页面后重试");
            }
        }
    }

    /* ==================== 内部工具 ==================== */

    /** 前端传的是书名和姓名，库里存的是 bid / sid，这一层负责翻译 */
    private void resolveKeys(Borrow borrow, BorrowMapper mapper) {
        String title = trimToNull(borrow.getTitle());
        String borrower = trimToNull(borrow.getBorrower());

        if (title != null) {
            Integer bid = mapper.findBidByTitle(title);
            if (bid == null) {
                throw new IllegalArgumentException("图书《" + title + "》不存在，请检查书名");
            }
            borrow.setBid(bid);
        }
        if (borrower != null) {
            Integer sid = mapper.findSidByName(borrower);
            if (sid == null) {
                throw new IllegalArgumentException("学生「" + borrower + "」不存在，请检查姓名");
            }
            borrow.setSid(sid);
        }
        if (borrow.getBid() == null) {
            throw new IllegalArgumentException("请选择图书");
        }
        if (borrow.getSid() == null) {
            throw new IllegalArgumentException("请选择借阅人");
        }
        if (borrow.getDuedate() == null) {
            throw new IllegalArgumentException("请填写应还日期");
        }
    }

    /** 有归还日期 → 已还；过了应还日期 → 逾期；其余 → 在借 */
    private String computeStatus(Borrow borrow) {
        if (borrow.getReturndate() != null) {
            return STATUS_RETURNED;
        }
        if (borrow.getDuedate() != null && borrow.getDuedate().isBefore(LocalDate.now())) {
            return STATUS_OVERDUE;
        }
        return STATUS_BORROWED;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
