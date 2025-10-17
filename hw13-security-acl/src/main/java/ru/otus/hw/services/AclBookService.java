package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Role;

@Slf4j
@Service
@RequiredArgsConstructor
public class AclBookService {

    private final JdbcMutableAclService aclService;

    public void createDefaultAcl(Book book) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ObjectIdentity oid = new ObjectIdentityImpl(Book.class, book.getId());
        MutableAcl acl = aclService.createAcl(oid);

        var ownerSid = new PrincipalSid(auth);
        acl.setOwner(ownerSid);

        add(acl, BasePermission.READ, ownerSid);
        add(acl, BasePermission.WRITE, ownerSid);
        add(acl, BasePermission.DELETE, ownerSid);
        add(acl, BasePermission.ADMINISTRATION, ownerSid);

        var adminSid = new GrantedAuthoritySid(Role.ROLE_ADMIN.name());
        add(acl, BasePermission.READ, adminSid);
        add(acl, BasePermission.WRITE, adminSid);
        add(acl, BasePermission.DELETE, adminSid);
        add(acl, BasePermission.ADMINISTRATION, adminSid);

        aclService.updateAcl(acl);
    }

    public void deleteAcl(Long bookId) {
        ObjectIdentity oid = new ObjectIdentityImpl(Book.class, bookId);
        try {
            aclService.deleteAcl(oid, false);
        } catch (NotFoundException ignore) {
            log.debug("ACL entry not found for Book with id: {}", bookId);
        }
    }

    private static void add(MutableAcl acl, Permission permission, Sid sid) {
        acl.insertAce(acl.getEntries().size(), permission, sid, true);
    }
}
