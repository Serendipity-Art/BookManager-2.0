package com.bookmanager.service;

import com.bookmanager.entity.Borrow;

import java.util.List;

/**
 * 借阅记录 业务逻辑层
 * <p>
 * 约定：业务上不合法时抛 IllegalArgumentException，message 直接给用户看，
 * Servlet 层捕获后原样放进 {"success":false,"message":...} 返回前端。
 *
 * @author Serendipity
 * @date 2026/10/25
 */
public interface BorrowService {

    /** 展示借阅信息（含书名、借阅人） */
    List<Borrow> showAllBorrow();

    /** 新增借阅：前端只给书名/姓名，由本层翻译成 bid / sid */
    void addBorrow(Borrow borrow);

    /** 修改借阅记录 */
    void updateBorrow(Borrow borrow);

    /** 归还：把归还日期设为今天，状态置为「已还」 */
    void returnBorrow(Integer id);

    /** 删除借阅记录 */
    void deleteBorrow(Integer id);
}
