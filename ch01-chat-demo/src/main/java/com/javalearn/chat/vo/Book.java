package com.javalearn.chat.vo;

import java.time.LocalDate;


public record Book(String author, String bookName, LocalDate publishDate, String description) {
}
