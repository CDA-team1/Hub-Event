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

-- L'organisateur de démo est aussi affilié au club culture, pour pouvoir y organiser
-- un évènement (règle CU19 : l'évènement doit appartenir à un club de l'organisateur).
INSERT INTO affiliation (club_id, user_id) VALUES (2, 3);

-- --- Évènements de démo (COM-02) ---

-- 1) À venir, publié, club sportif : sert de support aux commentaires ci-dessous.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (1, 'Tournoi de badminton amical',
        'Tournoi ouvert à tous les niveaux, en simple et en double. Matériel fourni sur place, venez avec une tenue de sport. Un goûter convivial clôturera la journée.',
        'Gymnase municipal, 31000 Toulouse', '2026-12-05 18:00:00', '2026-12-05 21:00:00',
        5.00, 10.00, 16, 'PUBLISHED', 'SPORT', 3, 1);

-- 2) Terminé, club culture : montre des commentaires sur un évènement passé.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (2, 'Exposition photo : Toulouse, hier et aujourd''hui',
        'Une rétrospective photographique mêlant clichés d''archives et prises de vue contemporaines de la Ville Rose. Visite libre, entrée gratuite pour les affiliés.',
        'Médiathèque José Cabanis, 31000 Toulouse', '2026-08-10 10:00:00', '2026-08-10 18:00:00',
        0.00, 3.00, 40, 'FINISHED', 'CULTURE', 3, 2);

-- --- Commentaires de démo (COM-02) ---
-- Auteurs limités aux comptes ACTIFS (règle CU12 : compte actif requis) : le membre en
-- attente d'activation (id 4) n'en publie donc aucun.

-- Sur le tournoi de badminton (à venir)
INSERT INTO comments (id, content, author_id, event_id, created_at)
VALUES (1, 'Super initiative ! Je m''inscris avec grand plaisir, ça fait longtemps que je n''ai pas tenu une raquette :)',
        1, 1, '2026-09-10 14:00:00');

INSERT INTO comments (id, content, author_id, event_id, created_at)
VALUES (2, 'Belle initiative de l''équipe d''organisation. N''hésitez pas si vous avez besoin de bénévoles pour l''accueil.',
        2, 1, '2026-09-11 09:30:00');

INSERT INTO comments (id, content, author_id, event_id, created_at)
VALUES (3, 'Merci à tous pour votre enthousiasme ! Pensez à prévoir une bouteille d''eau, la salle peut vite chauffer.',
        3, 1, '2026-09-12 17:45:00');

-- Sur l'exposition photo (terminée)
INSERT INTO comments (id, content, author_id, event_id, created_at)
VALUES (4, 'Très belle exposition, j''ai adoré les clichés du Pont Neuf pris dans les années 60.',
        1, 2, '2026-08-11 10:15:00');

INSERT INTO comments (id, content, author_id, event_id, created_at)
VALUES (5, 'Dommage d''avoir manqué le vernissage, est-ce qu''une captation vidéo sera mise en ligne ?',
        2, 2, '2026-08-11 19:00:00');

INSERT INTO comments (id, content, author_id, event_id, created_at)
VALUES (6, 'Merci à toutes les personnes venues nombreuses ! On prépare déjà la suite pour l''année prochaine.',
        3, 2, '2026-08-12 08:00:00');
