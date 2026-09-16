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

    /**
     * Vérifie si un autre utilisateur (identifiant différent) possède déjà l'adresse email
     * donnée. Utilisé lors d'une modification (CU13, CPT-05) pour ne rejeter l'email que s'il
     * est pris par un <strong>autre</strong> compte que celui en cours de modification.
     *
     * @param email l'email à vérifier
     * @param id    l'identifiant à exclure de la vérification
     * @return {@code true} si un autre compte possède déjà cet email
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Recherche un utilisateur par son jeton d'activation (CU6).
     *
     * @param activationToken le jeton reçu dans le lien d'activation
     * @return un {@link Optional} contenant l'utilisateur s'il existe
     */
    Optional<User> findByActivationToken(String activationToken);

    /**
     * Recherche un utilisateur par son jeton de confirmation de changement de mot de passe
     * (CU13).
     *
     * @param passwordChangeToken le jeton reçu dans le lien de confirmation
     * @return un {@link Optional} contenant l'utilisateur s'il existe
     */
    Optional<User> findByPasswordChangeToken(String passwordChangeToken);
}
