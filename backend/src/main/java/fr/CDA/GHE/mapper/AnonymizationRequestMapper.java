package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.entity.AnonymizationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit les demandes d'anonymisation entre l'entité JPA {@link AnonymizationRequest} et
 * le DTO {@link AnonymizationDto}.
 */
@Component
public class AnonymizationRequestMapper {

    private final UserMapper userMapper;

    public AnonymizationRequestMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * Convertit une demande d'anonymisation en DTO.
     *
     * @param request demande à convertir
     * @return DTO correspondant à la demande
     */
    public AnonymizationDto toDto(AnonymizationRequest request) {
        return new AnonymizationDto(
                request.getId(),
                userMapper.toDto(request.getUser()),
                request.getStatus(),
                request.getRequestDate()
        );
    }

    /**
     * Convertit une liste de demandes d'anonymisation en liste de DTO.
     *
     * @param requests demandes à convertir
     * @return la liste des DTO correspondants
     */
    public List<AnonymizationDto> toDtoList(List<AnonymizationRequest> requests) {
        return requests.stream().map(this::toDto).toList();
    }
}
