package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Book testBook;

    private Comment testComment;

    @BeforeEach
    void setUp() {
        var author = new Author(1L, "Test Author");
        var genre = new Genre(1L, "Test Genre");
        testBook = new Book(1L, "Test Book", author, List.of(genre));
        testComment = new Comment(1L, "Test comment", testBook, LocalDateTime.now());
    }

    @Test
    @DisplayName("should find comment by id with book relation")
    void shouldFindById() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        var result = commentService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getText()).isEqualTo("Test comment");
        assertThat(result.get().getBook()).isNotNull();
        assertThat(result.get().getBook().getTitle()).isEqualTo("Test Book");
    }

    @Test
    @DisplayName("should return empty when comment not found")
    void shouldReturnEmptyWhenNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        var result = commentService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should find all comments for a specific book")
    void shouldFindAllByBookId() {
        var comment2 = new Comment(2L, "Another comment", testBook, LocalDateTime.now());
        when(commentRepository.findAllByBookId(1L)).thenReturn(List.of(testComment, comment2));

        var result = commentService.findAllByBookId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Comment::getText)
                .containsExactly("Test comment", "Another comment");
        result.forEach(comment -> 
                assertThat(comment.getBook().getId()).isEqualTo(1L));
    }

    @Test
    @DisplayName("should successfully create comment with valid book")
    void shouldCreateComment() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        var result = commentService.create(1L, "New comment");

        assertThat(result).isNotNull();
        assertThat(result.getBook()).isEqualTo(testBook);
        
        var captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        var savedComment = captor.getValue();
        assertThat(savedComment.getText()).isEqualTo("New comment");
        assertThat(savedComment.getBook()).isEqualTo(testBook);
        assertThat(savedComment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should throw when creating comment for non-existent book")
    void shouldThrowWhenCreatingWithMissingBook() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(999L, "Comment text"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("should successfully update existing comment")
    void shouldUpdateComment() {
        var updatedComment = new Comment(1L, "Updated text", testBook, testComment.getCreatedAt());
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);

        var result = commentService.update(1L, "Updated text");

        assertThat(result).isNotNull();
        verify(commentRepository).save(any(Comment.class));
        assertThat(testComment.getText()).isEqualTo("Updated text");
    }

    @Test
    @DisplayName("should throw when updating non-existent comment")
    void shouldThrowWhenUpdatingMissingComment() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(999L, "New text"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment with id 999 not found");
    }

    @Test
    @DisplayName("should delete comment by id")
    void shouldDeleteById() {
        commentService.deleteById(1L);

        verify(commentRepository).deleteById(1L);
    }
}
