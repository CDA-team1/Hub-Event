package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.RegistrationDto;
import fr.CDA.GHE.entity.Registration;
import org.springframework.stereotype.Component;

/**
 * Convertit les inscriptions entre l'entité JPA {@link Registration} et le DTO {@link RegistrationDto}.
 */
@Component
public class RegistrationMapper {

    /**
     * Convertit une inscription en DTO.
     *
     * @param registration inscription à convertir
     * @return DTO correspondant à l'inscription
     */
    public RegistrationDto toDto(Registration registration) {
        return new RegistrationDto(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getUser().getId(),
                registration.getUser().getEmail(),
                registration.getStatus(),
                registration.getRegistrationDate()
        );
    }
}