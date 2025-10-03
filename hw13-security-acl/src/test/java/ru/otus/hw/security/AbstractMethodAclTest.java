package ru.otus.hw.security;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.stream.Stream;

@Transactional
abstract class AbstractMethodAclTest {

    @Autowired protected JdbcMutableAclService aclService;
    @Autowired private DataSource dataSource;

    /*
    * Dear maintainer:
    *
    * Прикол с H2. По дефолту `JdbcMutableAclService` пытался получать новые SID/Class ID через CALL IDENTITY(),
    * а в H2 такой функции нет, поэтому каждая вставка в ACL валились BadSqlGrammarException.
    * Починил, сделав одноразовую настройку `JdbcMutableAclService`:
    * для H2 подменяю sidIdentityQuery/classIdentityQuery на свои запросы.
    *
    * total_hours_wasted_here = 3
    * */
    @BeforeEach
    void configureIdentityQueries() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            var productName = connection.getMetaData().getDatabaseProductName();
            if ("H2".equalsIgnoreCase(productName)) {
                aclService.setSidIdentityQuery("select max(id) from acl_sid");
                aclService.setClassIdentityQuery("select max(id) from acl_class");
            }
        }
    }

    protected void authenticate(String username, String... roles) {
        var effectiveRoles = roles.length == 0 ? new String[] {"USER"} : roles;
        var authorities = Stream.of(effectiveRoles)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
        var authentication = new UsernamePasswordAuthenticationToken(username, "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected void grantPermission(Class<?> type, Long id, String username, Permission permission) {
        var previousAuth = SecurityContextHolder.getContext().getAuthentication();
        authenticate("acl-admin", "ADMIN");
        try {
            var identity = new ObjectIdentityImpl(type, id);
            MutableAcl acl = (MutableAcl) aclService.readAclById(identity);
            acl.insertAce(acl.getEntries().size(), permission, new PrincipalSid(username), true);
            aclService.updateAcl(acl);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previousAuth);
        }
    }
}
