package at.handoutmanager.backend.user;

import at.handoutmanager.backend.auth.AuthController;
import at.handoutmanager.backend.common.GlobalExceptionHandler;
import at.handoutmanager.backend.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturn201_whenRegistrationSucceeds() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "max@mustermann.com",
                                  "password": "pass1234",
                                  "userName": "Max"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn409_whenEmailIsAlreadyTaken() throws Exception {
        when(userService.register("max@mustermann.com", "pass1234", "Max"))
                .thenThrow(new EmailAlreadyExistsException("max@mustermann.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "max@mustermann.com",
                              "password": "pass1234",
                              "userName": "Max"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Registration failed."))
                .andExpect(jsonPath("$.detail").value(not(containsString("max@mustermann.com"))));
    }

    @Test
    void shouldReturn400_whenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "max@mustermann.com",
                                  "password": "kurz",
                                  "userName": "Max"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password")
                        .value("Password must be between 8 and 72 characters"));

        verify(userService, never()).register(any(), any(), any());
    }
}