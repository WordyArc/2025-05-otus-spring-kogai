package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.controllers.view.AuthorViewController;
import ru.otus.hw.controllers.view.BookViewController;
import ru.otus.hw.controllers.view.GenreViewController;
import ru.otus.hw.controllers.view.HomeViewController;
import ru.otus.hw.services.CustomUserDetailsService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BookViewController.class,
        AuthorViewController.class,
        GenreViewController.class,
        HomeViewController.class
})
@Import(SecurityConfig.class)
class ViewSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    class PublicPages {

        @Test
        @DisplayName("should allow access to login page")
        void shouldAllowAccessToLoginPage() throws Exception {
            mvc.perform(get("/login"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class UnauthenticatedUser {

        @Test
        @DisplayName("should redirect to login when accessing books")
        void shouldRedirectToLoginForBooks() throws Exception {
            mvc.perform(get("/books"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("should redirect to login when accessing authors")
        void shouldRedirectToLoginForAuthors() throws Exception {
            mvc.perform(get("/authors"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("should redirect to login when accessing genres")
        void shouldRedirectToLoginForGenres() throws Exception {
            mvc.perform(get("/genres"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("should redirect to login when accessing home")
        void shouldRedirectToLoginForHome() throws Exception {
            mvc.perform(get("/"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Nested
    class AuthenticatedUser {

        @Test
        @DisplayName("should allow access to books page")
        void shouldAllowAccessToBooks() throws Exception {
            mvc.perform(get("/books").with(user("testUser")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should allow access to authors page")
        void shouldAllowAccessToAuthors() throws Exception {
            mvc.perform(get("/authors").with(user("testUser")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should allow access to genres page")
        void shouldAllowAccessToGenres() throws Exception {
            mvc.perform(get("/genres").with(user("testUser")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should redirect home to books")
        void shouldRedirectHomeToBooks() throws Exception {
            mvc.perform(get("/").with(user("testUser")))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }
    }
}
