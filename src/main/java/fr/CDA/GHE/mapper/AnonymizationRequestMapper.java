package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.entity.AnonymizationRequest;
import org.springframework.stereotype.Component;

/**
 * Convertit les demandes d'anonymisation entre l'entité JPA {@link AnonymizationRequest} et
 * le DTO {@link AnonymizationDto}.
 */
@Component
public class AnonymizationRequestMapper {

    public AnonymizationDto toDto(AnonymizationRequest request) {
        return new AnonymizationDto(
                request.getId(),
                request.getUser().getEmail(),
                request.getStatus(),
                request.getRequestDate()
        );
    }
}
