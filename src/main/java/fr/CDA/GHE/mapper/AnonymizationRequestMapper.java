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

    public AnonymizationDto toDto(AnonymizationRequest request) {
        return new AnonymizationDto(
                request.getId(),
                userMapper.toDto(request.getUser()),
                request.getStatus(),
                request.getRequestDate()
        );
    }

    public List<AnonymizationDto> toDtoList(List<AnonymizationRequest> requests) {
        return requests.stream().map(this::toDto).toList();
    }
}
