package at.handoutmanager.backend.project;

import at.handoutmanager.backend.common.GlobalExceptionHandler;
import at.handoutmanager.backend.membership.MembershipRole;
import at.handoutmanager.backend.security.CorsProperties;
import at.handoutmanager.backend.security.JwtAuthenticationFilter;
import at.handoutmanager.backend.security.JwtProperties;
import at.handoutmanager.backend.security.JwtService;
import at.handoutmanager.backend.security.RestAuthenticationEntryPoint;
import at.handoutmanager.backend.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class})
class ProjectControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 2L;
    private static final String PROJECT_NAME = "Curse of Strahd";
    private static final String NICK_NAME = "Strahd von Zarovich";

    private static final String VALID_REQUEST = """
            {"name": "Curse of Strahd", "nickName": "Strahd von Zarovich"}
            """;
    private static final String BLANK_NAME_REQUEST = """
            {"name": "", "nickName": "Strahd von Zarovich"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectServiceMock;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private CorsProperties corsProperties;

    @Test
    void shouldReturnCreatedProject_whenRequestIsValid() throws Exception {
        when(projectServiceMock.createProject(USER_ID, PROJECT_NAME, NICK_NAME))
                .thenReturn(new ProjectSummary(PROJECT_ID, PROJECT_NAME, MembershipRole.GAME_MASTER));

        mockMvc.perform(post("/api/projects")
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PROJECT_ID))
                .andExpect(jsonPath("$.name").value(PROJECT_NAME))
                .andExpect(jsonPath("$.membershipRole").value("GAME_MASTER"));
    }

    @Test
    void shouldReturnBadRequest_whenProjectNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BLANK_NAME_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Project name is required"));

        verifyNoInteractions(projectServiceMock);
    }

    @Test
    void shouldReturnUnauthorized_whenNoTokenIsSent() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectServiceMock);
    }
}
