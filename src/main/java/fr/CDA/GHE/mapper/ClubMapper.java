package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.entity.Club;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit les clubs entre l'entité JPA {@link Club} et le DTO {@link ClubDto}.
 */

@Component
public class ClubMapper {

    public ClubDto toDto(Club club){
        return new ClubDto(
                club.getId(),
                club.getName(),
                club.getCategory(),
                club.getPostalAddress(),
                club.getEmail(),
                club.getPhone(),
                club.getValidityEndDate()
        );
    }
    public List<ClubDto> toDtoList(List<Club> clubs){
        return clubs.stream().map(this::toDto).toList();
    }
}
