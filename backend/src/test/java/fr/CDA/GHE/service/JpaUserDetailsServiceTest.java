package fr.CDA.GHE.service;

import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests de {@link JpaUserDetailsService} avec une vraie base H2 (profil "test", voir
 * {@code GheApplicationTests}) : vérifie que l'entité {@link User}, une fois persistée,
 * sert correctement de principal Spring Security (username, autorité ROLE_x) — valide
 * SEC-04 de bout en bout, pas seulement en mémoire.
 * <p>
 * {@code @DataJpaTest} n'est pas encore disponible pour cette version de Spring Boot
 * (module non résolu en offline) : on charge donc tout le contexte, comme
 * {@code GheApplicationTests}. {@code @Transactional} annule les écritures après chaque
 * test.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class JpaUserDetailsServiceTest {

    @Autowired
    private JpaUserDetailsService jpaUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void loadUserByUsername_shouldReturnUser_whenEmailExists() {
        User user = new User();
        user.setEmail("john.doe@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPostalAddress("1 rue de Test");
        user.setPassword("hashed-password");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(Role.ORGANIZER);
        userRepository.save(user);

        UserDetails result = jpaUserDetailsService.loadUserByUsername("john.doe@test.com");

        assertThat(result.getUsername()).isEqualTo("john.doe@test.com");
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ORGANIZER");
    }

    @Test
    void loadUserByUsername_shouldThrow_whenEmailUnknown() {
        assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername("unknown@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
