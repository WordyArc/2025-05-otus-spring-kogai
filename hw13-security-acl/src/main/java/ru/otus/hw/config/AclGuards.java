package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.CommentRepository;

@Component("acl")
@RequiredArgsConstructor
public class AclGuards {

    private final CommentRepository comments;

    private final PermissionEvaluator evaluator;


    public boolean canUpdateComment(Long commentId) {
        return hasOnCommentBook(commentId, BasePermission.WRITE);
    }

    public boolean canDeleteComment(Long commentId) {
        return hasOnCommentBook(commentId, BasePermission.DELETE);
    }

    private boolean hasOnCommentBook(Long commentId, Permission permission) {
        return comments.findById(commentId)
                .map(c -> evaluator.hasPermission(auth(), c.getBook().getId(), Book.class.getName(), permission))
                .orElse(false);
    }

    public boolean canModifyComment(Authentication auth, Long commentId, Permission permission) {
        return comments.findById(commentId)
                .map(c -> evaluator.hasPermission(auth, c.getBook(), permission))
                .orElse(false);
    }

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
