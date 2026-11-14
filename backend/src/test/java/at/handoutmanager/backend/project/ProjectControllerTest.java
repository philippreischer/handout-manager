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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class})
class ProjectControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 2L;
    private static final Long SECOND_PROJECT_ID = 3L;
    private static final String PROJECT_NAME = "Curse of Strahd";
    private static final String NEW_PROJECT_NAME = "House of Lament";
    private static final String SECOND_PROJECT_NAME = "Tomb of Annihilation";
    private static final String NICK_NAME = "Strahd von Zarovich";

    private static final String VALID_REQUEST = """
            {"name": "Curse of Strahd", "nickName": "Strahd von Zarovich"}
            """;
    private static final String BLANK_NAME_REQUEST = """
            {"name": "", "nickName": "Strahd von Zarovich"}
            """;
    private static final String VALID_RENAME_REQUEST = """
            {"name": "House of Lament"}
            """;
    private static final String BLANK_NAME_RENAME_REQUEST = """
            {"name": ""}
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

    //createProject
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

    //getProjects
    @Test
    void shouldReturnProjects_whenUserHasProjects() throws Exception {
        when(projectServiceMock.getProjectsForUser(USER_ID)).thenReturn(List.of(
                new ProjectSummary(PROJECT_ID, PROJECT_NAME, MembershipRole.GAME_MASTER),
                new ProjectSummary(SECOND_PROJECT_ID, SECOND_PROJECT_NAME, MembershipRole.PLAYER)));

        mockMvc.perform(get("/api/projects")
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(PROJECT_ID))
                .andExpect(jsonPath("$[0].name").value(PROJECT_NAME))
                .andExpect(jsonPath("$[0].membershipRole").value("GAME_MASTER"))
                .andExpect(jsonPath("$[1].id").value(SECOND_PROJECT_ID))
                .andExpect(jsonPath("$[1].name").value(SECOND_PROJECT_NAME))
                .andExpect(jsonPath("$[1].membershipRole").value("PLAYER"));
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoProjects() throws Exception {
        when(projectServiceMock.getProjectsForUser(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/projects")
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnUnauthorized_whenNoTokenIsSentForOverview() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectServiceMock);
    }

    //getProject
    @Test
    void shouldReturnProject_whenUserIsMember() throws Exception {
        when(projectServiceMock.getProject(USER_ID, PROJECT_ID))
                .thenReturn(new ProjectSummary(PROJECT_ID, PROJECT_NAME, MembershipRole.PLAYER));

        mockMvc.perform(get("/api/projects/{projectId}", PROJECT_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PROJECT_ID))
                .andExpect(jsonPath("$.name").value(PROJECT_NAME))
                .andExpect(jsonPath("$.membershipRole").value("PLAYER"));
    }

    @Test
    void shouldReturnNotFound_whenServiceThrowsProjectNotFound() throws Exception {
        when(projectServiceMock.getProject(USER_ID, PROJECT_ID))
                .thenThrow(new ProjectNotFoundException("No membership found"));

        mockMvc.perform(get("/api/projects/{projectId}", PROJECT_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Project not found"));
    }

    @Test
    void shouldReturnUnauthorized_whenNoTokenIsSentForDetail() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}", PROJECT_ID))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectServiceMock);
    }

    //renameProject
    @Test
    void shouldReturnRenamedProject_whenRequestIsValid() throws Exception {
        when(projectServiceMock.renameProject(USER_ID, PROJECT_ID, NEW_PROJECT_NAME))
                .thenReturn(new ProjectSummary(PROJECT_ID, NEW_PROJECT_NAME, MembershipRole.GAME_MASTER));

        mockMvc.perform(patch("/api/projects/{projectId}/name", PROJECT_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RENAME_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PROJECT_ID))
                .andExpect(jsonPath("$.name").value(NEW_PROJECT_NAME))
                .andExpect(jsonPath("$.membershipRole").value("GAME_MASTER"));
    }

    @Test
    void shouldReturnBadRequest_whenNewProjectNameIsBlank() throws Exception {
        mockMvc.perform(patch("/api/projects/{projectId}/name", PROJECT_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BLANK_NAME_RENAME_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Project name is required"));

        verifyNoInteractions(projectServiceMock);
    }

    @Test
    void shouldReturnNotFound_whenServiceThrowsProjectNotFoundOnRename() throws Exception {
        when(projectServiceMock.renameProject(USER_ID, PROJECT_ID, NEW_PROJECT_NAME))
                .thenThrow(new ProjectNotFoundException("No membership found"));

        mockMvc.perform(patch("/api/projects/{projectId}/name", PROJECT_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RENAME_REQUEST))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Project not found"));
    }

    @Test
    void shouldReturnForbidden_whenServiceThrowsAccessDenied() throws Exception {
        when(projectServiceMock.renameProject(USER_ID, PROJECT_ID, NEW_PROJECT_NAME))
                .thenThrow(new ProjectAccessDeniedException("userId: 1, projectId: 2"));

        mockMvc.perform(patch("/api/projects/{projectId}/name", PROJECT_ID)
                        .with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RENAME_REQUEST))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access denied"));
    }

    @Test
    void shouldReturnUnauthorized_whenNoTokenIsSentForRename() throws Exception {
        mockMvc.perform(patch("/api/projects/{projectId}/name", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RENAME_REQUEST))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectServiceMock);
    }

}
