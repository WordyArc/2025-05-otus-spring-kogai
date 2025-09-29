package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("comments")
public class Comment {
    @Id
    private Long id;

    @Column("text")
    private String text;

    @Column("book_id")
    private Long bookId;

    @Column("created_at")
    private LocalDateTime createdAt;

}