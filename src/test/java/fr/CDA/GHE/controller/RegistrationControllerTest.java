package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import fr.CDA.GHE.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de {@link RegistrationController}, centrés sur {@code register} (CdC, CU9 : « membre
 * affilié ou non, organisateur, administrateur » peuvent s'inscrire — pas de restriction de
 * rôle, contrairement à l'ancienne décision SEC-02 qui limitait cette route au rôle MEMBER).
 * {@link RegistrationService} est mocké ici : la logique métier (dont le contrôle du statut
 * de l'événement) est couverte par {@code RegistrationServiceTest}.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Test
    void register_shouldReturn201_whenMember() throws Exception {
        RegistrationDto created = new RegistrationDto(1L, 100L, "member@test.fr",
                RegistrationStatus.REGISTERED, LocalDateTime.of(2026, 9, 21, 10, 0));
        when(registrationService.register(100L)).thenReturn(created);

        mockMvc.perform(post("/events/100/registrations")
                        .with(user("member@test.fr").roles("MEMBER")))
                .andExpect(status().isCreated());
    }

    @Test
    void register_shouldReturn201_whenOrganizer() throws Exception {
        RegistrationDto created = new RegistrationDto(1L, 100L, "organizer@test.fr",
                RegistrationStatus.REGISTERED, LocalDateTime.of(2026, 9, 21, 10, 0));
        when(registrationService.register(100L)).thenReturn(created);

        mockMvc.perform(post("/events/100/registrations")
                        .with(user("organizer@test.fr").roles("ORGANIZER")))
                .andExpect(status().isCreated());
    }

    @Test
    void register_shouldReturn201_whenAdmin() throws Exception {
        RegistrationDto created = new RegistrationDto(1L, 100L, "admin@test.fr",
                RegistrationStatus.REGISTERED, LocalDateTime.of(2026, 9, 21, 10, 0));
        when(registrationService.register(100L)).thenReturn(created);

        mockMvc.perform(post("/events/100/registrations")
                        .with(user("admin@test.fr").roles("ADMIN")))
                .andExpect(status().isCreated());
    }

    @Test
    void register_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/events/100/registrations"))
                .andExpect(status().isUnauthorized());

        verify(registrationService, never()).register(any());
    }
}
