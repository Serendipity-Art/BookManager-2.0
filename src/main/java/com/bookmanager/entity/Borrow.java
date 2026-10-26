package com.bookmanager.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/25
 */
@Data
@Accessors(chain = true)
public class Borrow {
    @JsonProperty("borrowId")
    private Integer id;

    @JsonProperty("borrowDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate borrowdate;

    @JsonProperty("dueDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate duedate;

    @JsonProperty("returnDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returndate;

    @JsonProperty("status")
    private String borrowstatus;

    // ★ join 出来的展示字段
    @JsonProperty("title")
    private String title;

    @JsonProperty("borrower")
    private String borrower;

    private Integer sid;
    private Integer bid;
}

