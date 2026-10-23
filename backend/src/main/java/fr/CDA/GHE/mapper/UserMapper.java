package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.UserDto;
import fr.CDA.GHE.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit l'entité {@link User} vers son DTO d'exposition.
 * <p>
 * Pas de sens inverse ({@code toEntity}) : la création (CU5) construit l'entité dans le
 * service, qui seul connaît les règles métier (statut/rôle forcés, hash du mot de passe) —
 * un mapper générique masquerait ces décisions.
 */
@Component
public class UserMapper {

    /**
     * @param user entité à convertir
     * @return le DTO correspondant (sans le mot de passe)
     */
    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getLastName(),
                user.getFirstName(),
                user.getPostalAddress(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getRole()
        );
    }

    /**
     * @param users entités à convertir
     * @return la liste des DTO correspondants
     */
    public List<UserDto> toDtoList(List<User> users) {
        return users.stream().map(this::toDto).toList();
    }
}
