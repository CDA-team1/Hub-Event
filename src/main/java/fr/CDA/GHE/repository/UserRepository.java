package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email l'email à rechercher
     * @return un {@link Optional} contenant l'utilisateur s'il existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un utilisateur existe avec l'adresse email donnée.
     *
     * @param email l'email à vérifier
     * @return {@code true} si un utilisateur possède cet email
     */
    boolean existsByEmail(String email);
}
