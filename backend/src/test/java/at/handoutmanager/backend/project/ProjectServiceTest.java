package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.MembershipRole;
import at.handoutmanager.backend.membership.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 2L;
    private static final String PROJECT_NAME = "Curse of Strahd";
    private static final String NICK_NAME = "Strahd von Zarovich";

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private MembershipService membershipServiceMock;

    @InjectMocks
    private ProjectService sut;

    @Captor
    private ArgumentCaptor<Project> projectCaptor;

    private Project savedProject;

    @BeforeEach
    void setUp() {
        savedProject = new Project(PROJECT_NAME);
        ReflectionTestUtils.setField(savedProject, "id", PROJECT_ID);
    }

    @Test
    void shouldSaveProjectWithGivenName_whenProjectIsCreated() {
        when(projectRepositoryMock.save(any(Project.class))).thenReturn(savedProject);

        sut.createProject(USER_ID, PROJECT_NAME, NICK_NAME);

        verify(projectRepositoryMock).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getName()).isEqualTo(PROJECT_NAME);
    }

    @Test
    void shouldCreateGameMasterMembership_whenProjectIsCreated() {
        when(projectRepositoryMock.save(any(Project.class))).thenReturn(savedProject);

        sut.createProject(USER_ID, PROJECT_NAME, NICK_NAME);

        verify(membershipServiceMock).createMembership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.GAME_MASTER);
    }

    @Test
    void shouldReturnSummary_whenProjectIsCreated() {
        when(projectRepositoryMock.save(any(Project.class))).thenReturn(savedProject);

        ProjectSummary result = sut.createProject(USER_ID, PROJECT_NAME, NICK_NAME);

        assertThat(result).isEqualTo(new ProjectSummary(PROJECT_ID, PROJECT_NAME, MembershipRole.GAME_MASTER));
    }
}
