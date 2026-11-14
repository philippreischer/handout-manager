package at.handoutmanager.backend.project;

import at.handoutmanager.backend.membership.Membership;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 2L;
    private static final Long SECOND_PROJECT_ID = 3L;
    private static final String PROJECT_NAME = "Curse of Strahd";
    private static final String NEW_PROJECT_NAME = "House of Lament";
    private static final String SECOND_PROJECT_NAME = "Tomb of Annihilation";
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

    //createProject
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

    //getProjectsForUser
    @Test
    void shouldReturnSummariesWithNameFromProjectAndRoleFromMembership_whenUserHasProjects() {
        Project secondProject = projectWithId(SECOND_PROJECT_ID, SECOND_PROJECT_NAME);
        when(membershipServiceMock.getMembershipsByUserId(USER_ID)).thenReturn(List.of(
                new Membership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.GAME_MASTER),
                new Membership(USER_ID, SECOND_PROJECT_ID, NICK_NAME, MembershipRole.PLAYER)));
        when(projectRepositoryMock.findAllById(List.of(PROJECT_ID, SECOND_PROJECT_ID)))
                .thenReturn(List.of(secondProject, savedProject));

        List<ProjectSummary> result = sut.getProjectsForUser(USER_ID);

        assertThat(result).containsExactly(
                new ProjectSummary(PROJECT_ID, PROJECT_NAME, MembershipRole.GAME_MASTER),
                new ProjectSummary(SECOND_PROJECT_ID, SECOND_PROJECT_NAME, MembershipRole.PLAYER));
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoMemberships() {
        List<ProjectSummary> result = sut.getProjectsForUser(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldLoadAllProjectsInOneQuery_whenUserHasSeveralProjects() {
        Project secondProject = projectWithId(SECOND_PROJECT_ID, SECOND_PROJECT_NAME);
        when(membershipServiceMock.getMembershipsByUserId(USER_ID)).thenReturn(List.of(
                new Membership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.GAME_MASTER),
                new Membership(USER_ID, SECOND_PROJECT_ID, NICK_NAME, MembershipRole.GAME_MASTER)));
        when(projectRepositoryMock.findAllById(List.of(PROJECT_ID, SECOND_PROJECT_ID)))
                .thenReturn(List.of(savedProject, secondProject));

        sut.getProjectsForUser(USER_ID);

        verify(projectRepositoryMock, times(1)).findAllById(any());
        verify(projectRepositoryMock, never()).findById(any());
    }

    //getProject
    @Test
    void shouldReturnSummaryWithRoleFromMembership_whenUserIsMember() {
        when(membershipServiceMock.findMembership(USER_ID, PROJECT_ID))
                .thenReturn(Optional.of(new Membership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.PLAYER)));
        when(projectRepositoryMock.findById(PROJECT_ID)).thenReturn(Optional.of(savedProject));

        ProjectSummary result = sut.getProject(USER_ID, PROJECT_ID);

        assertThat(result).isEqualTo(new ProjectSummary(PROJECT_ID, PROJECT_NAME, MembershipRole.PLAYER));
    }

    @Test
    void shouldThrowProjectNotFound_whenUserIsNotMember() {
        when(membershipServiceMock.findMembership(USER_ID, PROJECT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getProject(USER_ID, PROJECT_ID))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepositoryMock, never()).findById(any());
    }

    @Test
    void shouldThrowProjectNotFound_whenProjectDoesNotExist() {
        when(membershipServiceMock.findMembership(USER_ID, PROJECT_ID))
                .thenReturn(Optional.of(new Membership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.GAME_MASTER)));
        when(projectRepositoryMock.findById(PROJECT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getProject(USER_ID, PROJECT_ID))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    //renameProject
    @Test
    void shouldReturnSummaryWithNewName_whenUserIsOwner(){
        when(membershipServiceMock.findMembership(USER_ID, PROJECT_ID))
                .thenReturn(Optional.of(new Membership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.GAME_MASTER)));
        when(projectRepositoryMock.findById(PROJECT_ID)).thenReturn(Optional.of(savedProject));
        when(projectRepositoryMock.save(any(Project.class))).thenReturn(savedProject);

        ProjectSummary result = sut.renameProject(USER_ID, PROJECT_ID, NEW_PROJECT_NAME);

        assertThat(result).isEqualTo(new ProjectSummary(PROJECT_ID, NEW_PROJECT_NAME, MembershipRole.GAME_MASTER));
        verify(projectRepositoryMock).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getName()).isEqualTo(NEW_PROJECT_NAME);
    }

    @Test
    void shouldThrowProjectNotFound_whenUserIsNotMemberOnRename(){
        when(membershipServiceMock.findMembership(USER_ID, PROJECT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.renameProject(USER_ID, PROJECT_ID, NEW_PROJECT_NAME))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepositoryMock, never()).save(any());
    }

    @Test
    void shouldThrowAccessDenied_whenUserIsNotGameMaster(){
        when(membershipServiceMock.findMembership(USER_ID, PROJECT_ID))
            .thenReturn(Optional.of(new Membership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.PLAYER)));

        assertThatThrownBy(() -> sut.renameProject(USER_ID, PROJECT_ID, NEW_PROJECT_NAME))
                .isInstanceOf(ProjectAccessDeniedException.class);

        verify(projectRepositoryMock, never()).save(any());
    }


    private Project projectWithId(Long id, String name) {
        Project project = new Project(name);
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }
}
