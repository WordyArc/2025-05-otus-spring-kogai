package ru.otus.hw.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionCacheOptimizer;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableCaching
@EnableMethodSecurity
public class AclConfig {

    @Bean
    public AclCache aclCache(CacheManager cacheManager,
                             PermissionGrantingStrategy permissionGrantingStrategy,
                             AclAuthorizationStrategy authorizationStrategy) {
        var cache = Objects.requireNonNull(
                cacheManager.getCache("aclCache"),
                "Cache 'aclCache' is not configured"
        );
        return new SpringCacheBasedAclCache(cache, permissionGrantingStrategy, authorizationStrategy);
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public LookupStrategy lookupStrategy(DataSource dataSource,
                                         AclCache cache,
                                         AclAuthorizationStrategy authorizationStrategy,
                                         PermissionGrantingStrategy permissionStrategy) {
        return new BasicLookupStrategy(dataSource, cache, authorizationStrategy, permissionStrategy);
    }

    @Bean
    public JdbcMutableAclService mutableAclService(DataSource dataSource,
                                               LookupStrategy lookupStrategy,
                                               AclCache cache) {
        return new JdbcMutableAclService(dataSource, lookupStrategy, cache);
    }

    @Bean
    public PermissionEvaluator permissionEvaluator(JdbcMutableAclService aclService) {
        return new AclPermissionEvaluator(aclService);
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator evaluator,
                                                                           JdbcMutableAclService aclService) {
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(evaluator);
        handler.setPermissionCacheOptimizer(new AclPermissionCacheOptimizer(aclService));
        return handler;
    }

}
