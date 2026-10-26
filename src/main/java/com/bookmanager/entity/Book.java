package com.bookmanager.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/23
 */
@Data
@Accessors(chain=true,fluent = true)
public class Book {
    Integer bid;
    String title;
    String author;
    Integer price;
    String description;
}
