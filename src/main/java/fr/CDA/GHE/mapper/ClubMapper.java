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
    public List<ClubDto> toDtoList(List<Club> clubs){
        return clubs.stream().map(this::toDto).toList();
    }
}
