package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.MemberSummaryDto;
import fr.CDA.GHE.entity.Club;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit les clubs entre l'entité JPA {@link Club} et le DTO {@link ClubDto}.
 */

@Component
public class ClubMapper {

    /**
     * Convertit un club en DTO, avec le résumé de ses membres affiliés.
     *
     * @param club club à convertir
     * @return DTO correspondant au club
     */
    public ClubDto toDto(Club club){
        List<MemberSummaryDto> members = club.getMembers().stream()
                .map(user -> new MemberSummaryDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail()))
                .toList();

        return new ClubDto(
                club.getId(),
                club.getName(),
                club.getCategory(),
                club.getPostalAddress(),
                club.getEmail(),
                club.getPhone(),
                club.getValidityEndDate(),
                members
        );
    }

    /**
     * Convertit une liste de clubs en liste de DTO.
     *
     * @param clubs clubs à convertir
     * @return la liste des DTO correspondants
     */
    public List<ClubDto> toDtoList(List<Club> clubs){
        return clubs.stream().map(this::toDto).toList();
    }
}
