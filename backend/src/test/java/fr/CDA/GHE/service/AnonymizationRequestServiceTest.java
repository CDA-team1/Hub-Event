package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.AdminAnonymizationDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.entity.AnonymizationRequest;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.RequestStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.AnonymizationRequestRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AnonymizationRequestServiceTest {

    @Autowired
    private AnonymizationRequestService anonymizationRequestService;

    @Autowired
    private AnonymizationRequestRepository anonymizationRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Test
    void extractAll_shouldReturnOnlyPendingRequests() {
        User pendingUser = persistUser("pending@test.com");
        User validatedUser = persistUser("validated@test.com");

        anonymizationRequestRepository.save(
                new AnonymizationRequest(
                        pendingUser,
                        RequestStatus.PENDING,
                        LocalDateTime.now()
                )
        );

        anonymizationRequestRepository.save(
                new AnonymizationRequest(
                        validatedUser,
                        RequestStatus.VALIDATED,
                        LocalDateTime.now()
                )
        );

        PageDto<AdminAnonymizationDto> result =
                anonymizationRequestService.extractAll(PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().status()).isEqualTo(RequestStatus.PENDING);
        assertThat(result.content().getFirst().user().email()).isEqualTo("pending@test.com");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    private User persistUser(String email) {
        User user = new User(
                "Doe",
                "Jane",
                "1 rue de Test",
                email,
                "0600000000",
                "hashed-password",
                Role.MEMBER
        );
        user.setStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Test
    void extractAll_shouldIncludeAffiliatedClubs() {
         User user = persistUser("affiliated@test.com");

        Club club = new Club(
                "Club Test",
                Category.SPORT,
                "10 rue du Club",
                "club@test.com",
                "0611111111"
        );

        club.getMembers().add(user);
        clubRepository.save(club);

        anonymizationRequestRepository.save(
                 new AnonymizationRequest(
                         user,
                         RequestStatus.PENDING,
                        LocalDateTime.now()
                )
        );

        PageDto<AdminAnonymizationDto> result =
                anonymizationRequestService.extractAll(PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().user().clubs()).hasSize(1);
        assertThat(result.content().getFirst().user().clubs().getFirst().name())
                .isEqualTo("Club Test");
        }
}