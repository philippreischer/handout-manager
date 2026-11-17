package at.handoutmanager.backend.membership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 2L;
    private static final String NICK_NAME = "Strahd von Zarovich";
    private static final String BLANK_NICK_NAME = "   ";
    private static final String NICK_NAME_WITH_SURROUNDING_SPACES = "  Strahd von Zarovich  ";

    @Mock
    private MembershipRepository membershipRepositoryMock;

    @InjectMocks
    private MembershipService sut;

    @Captor
    private ArgumentCaptor<Membership> membershipCaptor;

    @Test
    void shouldStoreNullNickName_whenNickNameIsNull() {
        sut.createMembership(USER_ID, PROJECT_ID, null, MembershipRole.GAME_MASTER);

        verify(membershipRepositoryMock).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getNickName()).isNull();
    }

    @Test
    void shouldStoreNullNickName_whenNickNameIsBlank() {
        sut.createMembership(USER_ID, PROJECT_ID, BLANK_NICK_NAME, MembershipRole.GAME_MASTER);

        verify(membershipRepositoryMock).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getNickName()).isNull();
    }

    @Test
    void shouldKeepNickNameUnchanged_whenNickNameHasSurroundingSpaces() {
        sut.createMembership(USER_ID, PROJECT_ID, NICK_NAME_WITH_SURROUNDING_SPACES, MembershipRole.GAME_MASTER);

        verify(membershipRepositoryMock).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getNickName()).isEqualTo(NICK_NAME_WITH_SURROUNDING_SPACES);
    }

    @Test
    void shouldAssignUserIdAndProjectId_whenMembershipIsCreated() {
        sut.createMembership(USER_ID, PROJECT_ID, NICK_NAME, MembershipRole.GAME_MASTER);

        verify(membershipRepositoryMock).save(membershipCaptor.capture());
        Membership savedMembership = membershipCaptor.getValue();
        assertThat(savedMembership.getUserId()).isEqualTo(USER_ID);
        assertThat(savedMembership.getProjectId()).isEqualTo(PROJECT_ID);
        assertThat(savedMembership.getNickName()).isEqualTo(NICK_NAME);
        assertThat(savedMembership.getMembershipRole()).isEqualTo(MembershipRole.GAME_MASTER);
    }

}
