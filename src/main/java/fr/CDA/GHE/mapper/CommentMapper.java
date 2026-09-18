package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.CommentDto;
import fr.CDA.GHE.entity.Comment;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit l'entité {@link Comment} vers son DTO d'exposition.
 */
@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getEvent().getId(),
                formatAuthorDisplayName(comment.getUser()),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    public List<CommentDto> toDtoList(List<Comment> comments) {
        return comments.stream().map(this::toDto).toList();
    }

    /**
     * Formate le nom d'affichage de l'auteur (CU12, règles métier n°4 et 5) : « Prénom +
     * première lettre du nom » pour un auteur normal, « SUPPRIMÉ » si l'auteur a depuis été
     * anonymisé — sans quoi le nom aléatoire généré par l'anonymisation apparaîtrait tel quel.
     */
    private String formatAuthorDisplayName(User author) {
        if (author.getStatus() == AccountStatus.ANONYMIZED) {
            return "SUPPRIMÉ";
        }

        String lastNameInitial = author.getLastName() == null || author.getLastName().isBlank()
                ? ""
                : author.getLastName().substring(0, 1).toUpperCase() + ".";

        return (author.getFirstName() + " " + lastNameInitial).trim();
    }
}
