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

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(nullable = false)
        private String lastname;
        @Column(nullable = false)
        private String firstname;
        @Column(nullable = false)
        private String postalAddress;
        @Column(nullable = false, unique = true)
        private String email;
        private String phone;
        @Column(nullable = false)
        private String password;
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private AccountStatus status;
        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        private Role role;
        private boolean suspended = false;
        private LocalDate suspensionEndDate;
        private String suspensionReason;
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
        @ManyToMany(mappedBy = "members")
        private Set<Club> clubs = new HashSet<>();

        public User() {
        }

        /**
         * Constructeur complet pour initialiser un utilisateur avec tous ses attributs.
         *
         * @param id                identifiant unique
         * @param lastName          nom de famille
         * @param firstName         prénom
         * @param postalAddress     adresse postale
         * @param email             adresse email (unique)
         * @param phone             numéro de téléphone (nullable)
         * @param password          mot de passe encodé
         * @param status            statut du compte
         * @param role              rôle de l'utilisateur
         * @param suspended         indique si le compte est suspendu
         * @param suspensionEndDate date de fin de suspension (nullable)
         * @param suspensionReason  raison de la suspension (nullable)
         */
        public User(Long id, String lastName, String firstName, String postalAddress, String email, String phone, String password, AccountStatus status, Role role, boolean suspended, LocalDate suspensionEndDate, String suspensionReason) {
            this.id = id;
            this.lastname = lastName;
            this.firstname = firstName;
            this.postalAddress = postalAddress;
            this.email = email;
            this.phone = phone;
            this.password = password;
            this.status = status;
            this.role = role;
            this.suspended = suspended;
            this.suspensionEndDate = suspensionEndDate;
            this.suspensionReason = suspensionReason;
        }

        public Long getId() {
            return id;
        }

        public String getLastName() {
            return lastname;
        }

        public void setLastName(String lastName) {
            this.lastname = lastName;
        }

        public String getFirstName() {
            return firstname;
        }

        public void setFirstName(String firstName) {
            this.firstname = firstName;
        }

        public String getPostalAddress() {
            return postalAddress;
        }

        public void setPostalAddress(String postalAddress) {
            this.postalAddress = postalAddress;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public AccountStatus getStatus() {
            return status;
        }

        public void setStatus(AccountStatus status) {
            this.status = status;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public boolean isSuspended() {
            return suspended;
        }

        public void setSuspended(boolean suspended) {
            this.suspended = suspended;
        }

        public LocalDate getSuspensionEndDate() {
            return suspensionEndDate;
        }

        public void setSuspensionEndDate(LocalDate suspensionEndDate) {
            this.suspensionEndDate = suspensionEndDate;
        }

        public String getSuspensionReason() {
            return suspensionReason;
        }

        public void setSuspensionReason(String suspensionReason) {
            this.suspensionReason = suspensionReason;
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

        public Set<Club> getClubs() {
            return clubs;
        }

        public void addClub(Club club) {
            if (clubs.add(club)) {
                club.getMembers().add(this);
            }
        }

        public boolean removeClub(Club club) {
            boolean removed = clubs.remove(club);
            if (removed) {
                club.getMembers().remove(this);
            }
            return removed;
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
