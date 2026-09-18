package fr.CDA.GHE.entity;

import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un utilisateur de l'application.
 * Implémente {@link UserDetails} : c'est l'entité elle-même qui sert de principal
 * Spring Security (chargée telle quelle par JpaUserDetailsService).
 */
    @Entity
    @Table(name = "users")
    public class User implements UserDetails {

        /**
         * Identifiant unique de l'utilisateur.
         * Généré automatiquement par la base de données.
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /**
         * Nom de famille de l'utilisateur.
         */
        @Column(nullable = false)
        private String lastname;

        /**
         * Prénom de l'utilisateur.
         */
        @Column(nullable = false)
        private String firstname;

        /**
         * Adresse postale de l'utilisateur.
         */
        @Column(name = "postal_address", nullable = false)
        private String postalAddress;

        /**
         * Adresse email de l'utilisateur.
         * Elle doit être unique sur l'ensemble de la plateforme.
         */
        @Column(nullable = false, unique = true)
        private String email;

        /**
         * Numéro de téléphone de l'utilisateur.
         * Peut être {@code null}, notamment pour un membre non affilié.
         */
        private String phone;

        /**
         * Mot de passe encodé de l'utilisateur.
         */
        @Column(nullable = false)
        private String password;

        /**
         * Statut actuel du compte utilisateur.
         * Un nouveau compte est créé au statut INACTIVE.
         */
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private AccountStatus status;

        /**
         * Rôle de l'utilisateur sur la plateforme.
         */
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Role role;

        /**
         * Indique si le compte utilisateur est actuellement suspendu.
         * Un nouveau compte n'est pas suspendu par défaut.
         */
        @Column(nullable = false)
        private boolean suspended = false;

        /**
         * Date de fin de suspension du compte.
         * Cette valeur est {@code null} en l'absence de suspension temporaire.
         */
        @Column(name = "suspension_end_date")
        private LocalDate suspensionEndDate;

        /**
         * Motif de la suspension du compte.
         * Cette valeur est {@code null} lorsque le compte n'est pas suspendu.
         */
        @Column(name = "suspension_reason")
        private String suspensionReason;

        @ManyToMany(mappedBy = "members")
        private Set<Club> clubs = new HashSet<>();

        @OneToMany(mappedBy = "organizer")
        private Set<Event> organizedEvents = new HashSet<>();


        @OneToMany(mappedBy = "user")
        private Set<Registration> registrations = new HashSet<>();

        @OneToMany(mappedBy = "user")
        private Set<Comment> comments = new HashSet<>();

        @OneToMany(mappedBy = "user")
        private Set<AnonymizationRequest> anonymizationRequests = new HashSet<>();

        /**
         * Jeton d'activation envoyé par email à la création du compte (CU5/CU6).
         * {@code null} une fois le compte activé (jeton à usage unique).
         */
        @Column(name = "activation_token", unique = true)
        private String activationToken;
        /**
         * Nouveau mot de passe (déjà hashé), en attente de confirmation par email (CU13).
         * {@code null} tant qu'aucun changement de mot de passe n'est en cours.
         */
        @Column(name = "pending_password")
        private String pendingPassword;
        /**
         * Jeton de confirmation du changement de mot de passe, envoyé par email (CU13, CdC :
         * « Si je ne confirme pas en cliquant sur le lien, mon mot de passe n'est pas modifié »).
         * {@code null} tant qu'aucun changement de mot de passe n'est en cours.
         */
        @Column(name = "password_change_token", unique = true)
        private String passwordChangeToken;

        /**
         * Constructeur vide requis par JPA.
         */
        public User() {
        }

        /**
         * Constructeur complet pour initialiser un utilisateur avec tous ses attributs.
         *
         * @param lastname          nom de famille
         * @param firstname         prénom
         * @param postalAddress     adresse postale
         * @param email             adresse email (unique)
         * @param phone             numéro de téléphone (nullable)
         * @param password          mot de passe encodé
         * @param role              rôle de l'utilisateur
         */
        public User(
                String lastname,
                String firstname,
                String postalAddress,
                String email,
                String phone,
                String password,
                Role role
        ) {
            this.lastname = lastname;
            this.firstname = firstname;
            this.postalAddress = postalAddress;
            this.email = email;
            this.phone = phone;
            this.password = password;
            this.status = AccountStatus.INACTIVE;
            this.role = role;
            this.suspended = false;
        }

        /**
         * Retourne l'identifiant unique de l'utilisateur.
         *
         * @return l'identifiant de l'utilisateur
         */
        public Long getId() {
            return id;
        }

        /**
         * Retourne le nom de famille de l'utilisateur.
         *
         * @return le nom de famille
         */
        public String getLastName() {
            return lastname;
        }

        /**
         * Modifie le nom de famille de l'utilisateur.
         *
         * @param lastName nouveau nom de famille
         */
        public void setLastName(String lastName) {
            this.lastname = lastName;
        }

        /**
         * Retourne le prénom de l'utilisateur.
         *
         * @return le prénom
         */
        public String getFirstName() {
            return firstname;
        }

        /**
         * Modifie le prénom de l'utilisateur.
         *
         * @param firstName nouveau prénom
         */
        public void setFirstName(String firstName) {
            this.firstname = firstName;
        }

        /**
         * Retourne l'adresse postale de l'utilisateur.
         *
         * @return l'adresse postale
         */
        public String getPostalAddress() {
            return postalAddress;
        }

        /**
         * Modifie l'adresse postale de l'utilisateur.
         *
         * @param postalAddress nouvelle adresse postale
         */
        public void setPostalAddress(String postalAddress) {
            this.postalAddress = postalAddress;
        }

        /**
         * Retourne l'adresse email de l'utilisateur.
         *
         * @return l'adresse email
         */
        public String getEmail() {
            return email;
        }

        /**
         * Modifie l'adresse email de l'utilisateur.
         *
         * @param email nouvelle adresse email
         */
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * Retourne le numéro de téléphone de l'utilisateur.
         *
         * @return le numéro de téléphone,
         *         ou {@code null} s'il n'est pas renseigné
         */
        public String getPhone() {
            return phone;
        }

        /**
         * Modifie le numéro de téléphone de l'utilisateur.
         *
         * @param phone nouveau numéro de téléphone,
         *              éventuellement {@code null}
         */
        public void setPhone(String phone) {
            this.phone = phone;
        }

        /**
         * Retourne le mot de passe encodé de l'utilisateur.
         *
         * @return le mot de passe encodé
         */
        public String getPassword() {
            return password;
        }

        /**
         * Modifie le mot de passe de l'utilisateur.
         *
         * @param password nouveau mot de passe, déjà encodé (jamais en clair)
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * Retourne le statut actuel du compte.
         *
         * @return le statut du compte
         */
        public AccountStatus getStatus() {
            return status;
        }

        /**
         * Modifie le statut du compte.
         *
         * @param status nouveau statut du compte
         */
        public void setStatus(AccountStatus status) {
            this.status = status;
        }

        /**
         * Active le compte de l'utilisateur (CU5/CU6).
         */
        public void activate() {
            this.status = AccountStatus.ACTIVE;
        }

        /**
         * Anonymise le compte de l'utilisateur.
         */
        public void anonymize() {
            this.status = AccountStatus.ANONYMIZED;
        }

        /**
         * Retourne le rôle de l'utilisateur.
         *
         * @return le rôle de l'utilisateur
         */
        public Role getRole() {
            return role;
        }

        /**
         * Modifie le rôle de l'utilisateur.
         *
         * @param role nouveau rôle de l'utilisateur
         */
        public void setRole(Role role) {
            this.role = role;
        }

        /**
         * Indique si le compte est actuellement suspendu.
         *
         * @return {@code true} si le compte est suspendu,
         *         {@code false} sinon
         */
        public boolean isSuspended() {
            return suspended;
        }

        /**
         * Modifie directement l'état de suspension du compte.
         *
         * @param suspended nouvel état de suspension
         */
        public void setSuspended(boolean suspended) {
            this.suspended = suspended;
        }

        /**
         * Retourne la date de fin de suspension.
         *
         * @return la date de fin de suspension,
         *         ou {@code null} lorsqu'elle n'existe pas
         */
        public LocalDate getSuspensionEndDate() {
            return suspensionEndDate;
        }

        /**
         * Modifie la date de fin de suspension.
         *
         * @param suspensionEndDate nouvelle date de fin,
         *                          éventuellement {@code null}
         */
        public void setSuspensionEndDate(LocalDate suspensionEndDate) {
            this.suspensionEndDate = suspensionEndDate;
        }

        /**
         * Retourne le motif de suspension du compte.
         *
         * @return le motif de suspension,
         *         ou {@code null} lorsqu'il n'existe pas
         */
        public String getSuspensionReason() {
            return suspensionReason;
        }

        /**
         * Modifie le motif de suspension du compte.
         *
         * @param suspensionReason nouveau motif,
         *                         éventuellement {@code null}
         */
        public void setSuspensionReason(String suspensionReason) {
            this.suspensionReason = suspensionReason;
        }

        public void suspendTemporarily(
                LocalDate suspensionEndDate,
                String suspensionReason
        ) {
            this.suspended = true;
            this.suspensionEndDate = suspensionEndDate;
            this.suspensionReason = suspensionReason;
        }

        public void suspendIndefinitely(String suspensionReason) {
            this.suspended = true;
            this.suspensionEndDate = null;
            this.suspensionReason = suspensionReason;
        }

        public void liftSuspension() {
            this.suspended = false;
            this.suspensionEndDate = null;
            this.suspensionReason = null;
        }

        public Set<Club> getClubs() {
            return clubs;
        }

        public void addClub(Club club) {
            clubs.add(club);
            club.getMembers().add(this);
        }

        public Set<Event> getOrganizedEvents() {
            return organizedEvents;
        }

        public void addOrganizedEvent(Event event) {
            organizedEvents.add(event);
            event.setOrganizer(this);
        }

        public Set<Registration> getRegistrations() {
            return registrations;
        }

        public void addRegistration(Registration registration) {
            registrations.add(registration);
            registration.setUser(this);
        }

        public Set<Comment> getComments() {
            return comments;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
            comment.setUser(this);
        }

        public Set<AnonymizationRequest> getAnonymizationRequests() {
            return anonymizationRequests;
        }

        public void addAnonymizationRequest(
                AnonymizationRequest anonymizationRequest
        ) {
            anonymizationRequests.add(anonymizationRequest);
            anonymizationRequest.setUser(this);
        }

        public String getActivationToken() {
            return activationToken;
        }

        public void setActivationToken(String activationToken) {
            this.activationToken = activationToken;
        }

        public String getPendingPassword() {
            return pendingPassword;
        }

        public void setPendingPassword(String pendingPassword) {
            this.pendingPassword = pendingPassword;
        }

        public String getPasswordChangeToken() {
            return passwordChangeToken;
        }

        public void setPasswordChangeToken(String passwordChangeToken) {
            this.passwordChangeToken = passwordChangeToken;
        }

        // --- Implémentation UserDetails ---
        // isAccountNonExpired/isAccountNonLocked/isCredentialsNonExpired/isEnabled renvoient
        // true : le statut métier (ACTIVE/INACTIVE/ANONYMIZED) et la suspension sont vérifiés
        // explicitement dans AuthService (messages d'erreur distincts), pas ici.

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(role);
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        // TODO : Event - organizedEvents(OneToMAny), Comment(OneToMany)
    }
