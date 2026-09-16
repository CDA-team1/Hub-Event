-- Jeu de données de démo (CPT-07), chargé au démarrage en dev (spring.sql.init.mode=always).
-- Remplace le mécanisme DataConfig/JpaUserDetailsService.initData() (SEC-06), retiré.
--
-- Mot de passe commun aux 4 comptes : Password123! (respecte les règles CU5 : 12 car. min,
-- majuscule, minuscule, chiffre, spécial). Hash BCrypt pré-calculé avec le même
-- BCryptPasswordEncoder que l'application (ne peut pas être généré en SQL brut).

-- --- Club de démo (nécessaire pour affilier un ORGANIZER, règle PO) ---
INSERT INTO clubs (id, name, category, postal_address, email, phone, validity_end_date)
VALUES (1, 'Club Démo Occitanie', 'SPORT', '1 rue de la Fédération, 31000 Toulouse',
        'contact@club-demo.local', '0500000000', NULL);

INSERT INTO clubs (id, name, category, postal_address, email, phone, validity_end_date)
VALUES (2, 'Club Culture Toulouse', 'CULTURE', '5 place du Capitole, 31000 Toulouse',
        'contact@club-culture-demo.local', '0500000001', NULL);

-- --- Comptes utilisateurs ---

-- 1) Membre non affilié, actif
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password,
                    status, role, suspended, activation_token)
VALUES (1, 'Doe', 'John', '1 rue de Test, 31000 Toulouse', 'member.demo@ghe.local', NULL,
        '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W',
        'ACTIVE', 'MEMBER', false, NULL);

-- 2) Administrateur, actif
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password,
                    status, role, suspended, activation_token)
VALUES (2, 'Alderson', 'Elliot', '1 rue de Test, 31000 Toulouse', 'admin.demo@ghe.local', NULL,
        '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W',
        'ACTIVE', 'ADMIN', false, NULL);

-- 3) Organisateur, actif, affilié au club de démo (obligatoire, règle PO)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password,
                    status, role, suspended, activation_token)
VALUES (3, 'Renoir', 'Pierre-Auguste', '1 rue de Test, 31000 Toulouse', 'organizer.demo@ghe.local', NULL,
        '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W',
        'ACTIVE', 'ORGANIZER', false, NULL);

INSERT INTO affiliation (club_id, user_id) VALUES (1, 3);

-- 4) Membre en attente d'activation (statut INACTIF, jeton connu pour tester le lien
--    d'activation manuellement, sans repasser par un vrai signup + email) :
--    GET http://localhost:8080/auth/activate?token=demo-activation-token-gustave
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password,
                    status, role, suspended, activation_token)
VALUES (4, 'Sacrebleu', 'Gustave', '1 rue de Test, 31000 Toulouse', 'pending.demo@ghe.local', NULL,
        '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W',
        'INACTIVE', 'MEMBER', false, 'demo-activation-token-gustave');

-- Le membre non affilié (id 1) devient affilié à 2 clubs, pour démontrer qu'un membre
-- peut être rattaché à plusieurs clubs (règle CU26 : "un membre peut être affilié à
-- plusieurs clubs ou n'être affilié à aucun club").
INSERT INTO affiliation (club_id, user_id) VALUES (1, 1);
INSERT INTO affiliation (club_id, user_id) VALUES (2, 1);
