-- Jeu de données de démo (CPT-07), chargé au démarrage en dev (spring.sql.init.mode=always).
-- Remplace le mécanisme DataConfig/JpaUserDetailsService.initData() (SEC-06), retiré.
--
-- Mot de passe commun à TOUS les comptes : Password123! (respecte les règles CU5 : 12 car. min,
-- majuscule, minuscule, chiffre, spécial). Hash BCrypt pré-calculé avec le même
-- BCryptPasswordEncoder que l'application (ne peut pas être généré en SQL brut).
--
-- Ce jeu de données couvre un maximum d'états, pour tester toute l'application sans rien saisir.
-- Comptes (tous en Password123!) :
--   1  member.demo@ghe.local                   membre actif, affilié à 2 clubs (1 et 2)
--   2  admin.demo@ghe.local                    administrateur
--   3  organizer.demo@ghe.local                organisateur (clubs 1 et 2), propriétaire des évènements 1 à 35, 37 et 40
--   4  pending.demo@ghe.local                  compte INACTIF, en attente d'activation (jeton ci-dessous)
--   5  organizer2.demo@ghe.local               organisateur du club de loisirs (3), autre propriétaire d'évènements
--   6  organizer3.demo@ghe.local               organisateur du club sportif breton (4), demande d'anonymisation EN ATTENTE
--   7  member2.demo@ghe.local                  membre actif affilié au club 1
--   8  member3.demo@ghe.local                  membre actif affilié au club 2
--   9  member4.demo@ghe.local                  membre actif affilié à 2 clubs (1 et 3)
--   10 nonaffilie.demo@ghe.local               membre actif NON affilié, sans téléphone
--   11 password.pending.demo@ghe.local         changement de mot de passe en attente de confirmation par email
--   12 anonymization.pending.demo@ghe.local    membre avec demande d'anonymisation EN ATTENTE
--   13 suspended.temp.demo@ghe.local           suspendu TEMPORAIREMENT (jusqu'au 2026-11-30)
--   14 suspended.forever.demo@ghe.local        suspendu DÉFINITIVEMENT
--   15 suspension.expired.demo@ghe.local       suspension EXPIRÉE (peut se connecter)
--   16 anonymized-...@ghe.local                compte ANONYMISÉ (demande validée), apparaît « SUPPRIMÉ » en commentaire
--   17 former.organizer.demo@ghe.local         ancien organisateur d'un club supprimé, redevenu simple membre
--   18 admin2.demo@ghe.local                   second administrateur

-- --- Club de démo (nécessaire pour affilier un ORGANIZER, règle PO) ---
INSERT INTO clubs (id, name, category, postal_address, email, phone, validity_end_date)
VALUES (1, 'Club Démo Occitanie', 'SPORT', '1 rue de la Fédération, 31000 Toulouse',
        'contact@club-demo.local', '0500000000', NULL);

INSERT INTO clubs (id, name, category, postal_address, email, phone, validity_end_date)
VALUES (2, 'Club Culture Toulouse', 'CULTURE', '5 place du Capitole, 31000 Toulouse',
        'contact@club-culture-demo.local', '0500000001', NULL);

-- Autres clubs : un club de loisirs, un second club sportif, et un club désaffilié (fin
-- d'affiliation par l'admin, CU24 : validity_end_date renseignée, plus aucun membre).
INSERT INTO clubs (id, name, category, postal_address, email, phone, validity_end_date)
VALUES (3, 'Club Loisirs Lyonnais', 'LEISURE', '12 quai Saint-Antoine, 69002 Lyon',
        'contact@club-loisirs-demo.local', '0500000002', NULL);

INSERT INTO clubs (id, name, category, postal_address, email, phone, validity_end_date)
VALUES (4, 'Association Sportive Bretonne', 'SPORT', '8 rue de la Monnaie, 35000 Rennes',
        'contact@asb-demo.local', '0500000003', NULL);

INSERT INTO clubs (id, name, category, postal_address, email, phone, validity_end_date)
VALUES (5, 'Ancien Club Escalade Grenoble', 'SPORT', '3 rue Lesdiguières, 38000 Grenoble',
        'contact@club-escalade-demo.local', '0500000004', '2026-06-30');

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

-- --- Comptes de démo supplémentaires : un compte par état à tester ---

-- 5) Second organisateur, club de loisirs (propriétaire des évènements 36 et 38)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (5, 'Lefèvre', 'Camille', '14 rue des Marronniers, 69003 Lyon', 'organizer2.demo@ghe.local', '0612345678', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'ORGANIZER', false);
-- 6) Organisateur du club sportif breton, avec une demande d’anonymisation en attente
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (6, 'Moreau', 'Hugo', '27 rue de Fougères, 35000 Rennes', 'organizer3.demo@ghe.local', '0623456789', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'ORGANIZER', false);
-- 7) Membre actif, affilié au club sportif
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (7, 'Bernard', 'Léa', '5 rue Pasteur, 31000 Toulouse', 'member2.demo@ghe.local', '0634567890', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', false);
-- 8) Membre actif, affilié au club culturel
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (8, 'Petit', 'Nathan', '19 avenue de Muret, 31300 Toulouse', 'member3.demo@ghe.local', '0645678901', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', false);
-- 9) Membre actif, affilié à deux clubs (sportif et loisirs)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (9, 'Roux', 'Inès', '2 place Bellecour, 69002 Lyon', 'member4.demo@ghe.local', '0656789012', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', false);
-- 10) Membre actif NON affilié, sans téléphone (tarif non affilié)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (10, 'Fournier', 'Lucas', '8 rue Nationale, 59800 Lille', 'nonaffilie.demo@ghe.local', NULL, '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', false);
-- 11) Changement de mot de passe demandé, en attente de confirmation par email :
--     GET http://localhost:8080/auth/confirm-password-change?token=demo-password-change-token-manon
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended, pending_password, password_change_token)
VALUES (11, 'Girard', 'Manon', '31 cours Mirabeau, 13100 Aix-en-Provence', 'password.pending.demo@ghe.local', '0667890123', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', false, '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'demo-password-change-token-manon');
-- 12) Membre actif ayant demandé son anonymisation (demande en attente, voir plus bas)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (12, 'Muller', 'Théo', '4 rue des Tanneurs, 35000 Rennes', 'anonymization.pending.demo@ghe.local', '0678901234', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', false);
-- 13) Suspension TEMPORAIRE, active jusqu’au 2026-11-30 (connexion refusée)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended, suspension_end_date, suspension_reason)
VALUES (13, 'Dubois', 'Chloé', '16 rue Sainte-Catherine, 33000 Bordeaux', 'suspended.temp.demo@ghe.local', '0689012345', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', true, '2026-11-30', 'Propos déplacés répétés dans les commentaires.');
-- 14) Suspension DÉFINITIVE (pas de date de fin, connexion refusée)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended, suspension_end_date, suspension_reason)
VALUES (14, 'Lambert', 'Antoine', '9 boulevard Gambetta, 06000 Nice', 'suspended.forever.demo@ghe.local', '0690123456', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', true, NULL, 'Non-respect répété des conditions d’utilisation.');
-- 15) Suspension EXPIRÉE : suspended = true mais date de fin dépassée, la connexion est autorisée
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended, suspension_end_date, suspension_reason)
VALUES (15, 'Garcia', 'Sofia', '22 rue du Faubourg, 34000 Montpellier', 'suspension.expired.demo@ghe.local', '0601234567', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', true, '2026-09-15', 'Comportement inapproprié lors d’un évènement.');
-- 16) Compte ANONYMISÉ (demande validée) : données remplacées par des valeurs aléatoires
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (16, 'a3f1c9d2-7b4e-4c8a-9e15-2d6f0b7a1c43', '5e8b2a71-0c3d-4f96-8a2e-91b7d4c6e038', '9c2d7e40-3a15-4b8f-b6d1-7e0a5f3c2b19', 'anonymized-d41e6b92-8f07-4a3c-b5e2-1c9a7d3f6e80@ghe.local', NULL, '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ANONYMIZED', 'MEMBER', false);
-- 17) Ancien organisateur du club supprimé (5) : redevenu membre, sans club (CU24)
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (17, 'Marchand', 'Victor', '11 rue Bayard, 38000 Grenoble', 'former.organizer.demo@ghe.local', '0612348765', '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'MEMBER', false);
-- 18) Second administrateur
INSERT INTO users (id, lastname, firstname, postal_address, email, phone, password, status, role, suspended)
VALUES (18, 'Morel', 'Julie', '1 rue de Test, 31000 Toulouse', 'admin2.demo@ghe.local', NULL, '$2a$10$Pi.OYCuDOiy9mLhmzcI/zeUQMM452QHMH5iFQm1HWNSp3KlvDZ57W', 'ACTIVE', 'ADMIN', false);

-- Affiliations des comptes ci-dessus (le club 5, désaffilié, n'a plus aucun membre).
INSERT INTO affiliation (club_id, user_id) VALUES (3, 5);
INSERT INTO affiliation (club_id, user_id) VALUES (4, 6);
INSERT INTO affiliation (club_id, user_id) VALUES (1, 7);
INSERT INTO affiliation (club_id, user_id) VALUES (2, 8);
INSERT INTO affiliation (club_id, user_id) VALUES (1, 9);
INSERT INTO affiliation (club_id, user_id) VALUES (3, 9);
INSERT INTO affiliation (club_id, user_id) VALUES (4, 12);
INSERT INTO affiliation (club_id, user_id) VALUES (2, 13);
INSERT INTO affiliation (club_id, user_id) VALUES (1, 15);

-- --- Évènements de démo (COM-02) ---

-- 1) À venir, publié, club sportif : sert de support aux commentaires ci-dessous.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (1, 'Tournoi de foot à 5 en nocturne',
        'Tournoi amical de foot à 5, sur terrain synthétique éclairé, en plein cœur de ville. Ouvert à tous niveaux, ambiance conviviale et sportive garantie. Un goûter clôturera la soirée.',
        'Complexe sportif de la Fontaine-Lestang, 31000 Toulouse', '2026-12-05 18:00:00', '2026-12-05 21:00:00',
        5.00, 10.00, 16, 'PUBLISHED', 'SPORT', 3, 1);

-- 2) Terminé, club culture : montre des commentaires sur un évènement passé.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (2, 'Exposition d''art contemporain : regards sur la ville',
        'Une exposition qui mêle sculptures contemporaines et peintures abstraites, dans un cadre lumineux avec vue sur la ville. Visite libre, entrée gratuite pour les affiliés.',
        'Médiathèque José Cabanis, 31000 Toulouse', '2026-08-10 10:00:00', '2026-08-10 18:00:00',
        0.00, 3.00, 40, 'FINISHED', 'CULTURE', 3, 2);

-- 3) À venir, publié, club culture.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (3, 'Concert acoustique au bord du lac',
        'Un concert acoustique en plein air, au coucher du soleil, au bord du lac de la Ramée. Ambiance intimiste garantie, food-trucks sur place. Places limitées, pensez à réserver.',
        'Base de loisirs de la Ramée, 31170 Tournefeuille', '2026-10-18 19:00:00', '2026-10-18 22:00:00',
        8.00, 15.00, 80, 'PUBLISHED', 'CULTURE', 3, 2);

-- 4) À venir, publié, club sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (4, 'Soirée jeux de société',
        'Une soirée conviviale autour de grands classiques et de découvertes ludiques, animée par des bénévoles du club. Boissons chaudes et grignotage offerts. Ouvert à tous niveaux, débutants bienvenus.',
        'Le Bocal, bar à jeux, 31000 Toulouse', '2026-11-14 19:30:00', '2026-11-14 23:00:00',
        3.00, 6.00, 20, 'PUBLISHED', 'LEISURE', 3, 1);

-- --- Évènements de démo supplémentaires (EVT-13) ---
-- 30 évènements (ids 5 à 34) répartis sur plusieurs régions, avec leur photo de preview ci-dessous.
-- Sport -> club 1, Culture -> club 2, Loisirs -> club 1 ; organisateur de démo (id 3).

-- 5) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (5, 'Soirée jeux au café ludique',
        'Retrouvez vos amis autour de jeux de société et de défis stratégiques dans une ambiance chaleureuse et conviviale.',
        'Café ludique Les Quatre As, 69001 Lyon', '2026-10-16 19:30:00', '2026-10-16 23:00:00',
        3.00, 6.00, 24, 'PUBLISHED', 'LEISURE', 3, 1);

-- 6) Terminé, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (6, 'Cinéma en plein air',
        'Profitez d’une projection en extérieur à la tombée de la nuit, confortablement installé dans une ambiance chaleureuse sous les guirlandes lumineuses.',
        'Parc des Coteaux, 33100 Bordeaux', '2026-08-29 21:00:00', '2026-08-29 23:30:00',
        0.00, 5.00, 150, 'FINISHED', 'CULTURE', 3, 2);

-- 7) À venir, publié, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (7, 'Exposition d’art contemporain',
        'Découvrez une sélection d’œuvres contemporaines mêlant peintures abstraites et sculptures dans une galerie moderne et lumineuse.',
        'Galerie du Quai, 44000 Nantes', '2026-11-12 10:00:00', '2026-11-12 19:00:00',
        0.00, 4.00, 60, 'PUBLISHED', 'CULTURE', 3, 2);

-- 8) Terminé, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (8, 'Concert de jazz au bord du lac',
        'Venez profiter d’un concert de jazz en plein air au coucher du soleil, dans un cadre paisible avec vue sur le lac.',
        'Esplanade du Pâquier, 74000 Annecy', '2026-09-12 19:00:00', '2026-09-12 22:30:00',
        10.00, 18.00, 120, 'FINISHED', 'CULTURE', 3, 2);

-- 9) À venir, publié, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (9, 'Marathon du centre-ville',
        'Rejoignez des centaines de coureurs pour une course au cœur de la ville, entre défi sportif, patrimoine et ambiance festive.',
        'Place Kléber, 67000 Strasbourg', '2026-10-25 08:30:00', '2026-10-25 13:30:00',
        15.00, 25.00, 500, 'PUBLISHED', 'SPORT', 3, 1);

-- 10) À venir, publié, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (10, 'Tournoi de futsal nocturne',
        'Formez votre équipe et participez à un tournoi de futsal en soirée sur terrain synthétique, dans une ambiance sportive et conviviale.',
        'Complexe sportif du Grand Stade, 59000 Lille', '2026-10-23 20:00:00', '2026-10-23 23:30:00',
        5.00, 10.00, 40, 'PUBLISHED', 'SPORT', 3, 1);

-- 11) À venir, publié, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (11, 'Yoga au lever du soleil',
        'Commencez la journée avec une séance de yoga en plein air face au lever du soleil. Activité accessible à tous les niveaux.',
        'Parc Borély, 13008 Marseille', '2026-10-11 07:45:00', '2026-10-11 09:15:00',
        0.00, 6.00, 30, 'PUBLISHED', 'SPORT', 3, 1);

-- 12) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (12, 'Escape Game : Mystère égyptien',
        'Résolvez des énigmes en équipe dans un décor inspiré de l’Égypte ancienne et tentez de percer les secrets d’une mystérieuse tombe.',
        'Escape Quest, 31000 Toulouse', '2026-11-07 14:00:00', '2026-11-07 17:00:00',
        12.00, 20.00, 18, 'PUBLISHED', 'LEISURE', 3, 1);

-- 13) Terminé, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (13, 'Brunch fleuri au jardin',
        'Profitez d’un brunch convivial dans un jardin fleuri et apprenez à composer votre propre bouquet dans une ambiance détendue.',
        'Mas des Roses, 84000 Avignon', '2026-09-13 10:30:00', '2026-09-13 14:00:00',
        18.00, 28.00, 24, 'FINISHED', 'LEISURE', 3, 1);

-- 14) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (14, 'Atelier de poterie',
        'Initiez-vous au modelage de l’argile et réalisez votre propre création en céramique lors d’un atelier convivial et accessible aux débutants.',
        'Atelier Terre et Feu, 35000 Rennes', '2026-11-21 14:00:00', '2026-11-21 17:30:00',
        20.00, 30.00, 12, 'PUBLISHED', 'LEISURE', 3, 1);

-- 15) À venir, publié, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (15, 'Initiation à l’escalade en bloc',
        'Découvrez l’escalade en salle sur des parcours adaptés à différents niveaux. Une séance dynamique pour apprendre les bases, progresser et relever de nouveaux défis en groupe.',
        'Salle de bloc Vertige, 38000 Grenoble', '2026-10-18 10:00:00', '2026-10-18 12:30:00',
        8.00, 14.00, 16, 'PUBLISHED', 'SPORT', 3, 1);

-- 16) Terminé, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (16, 'Paddle au coucher du soleil',
        'Partez pour une balade en stand-up paddle sur une eau calme et profitez des dernières lumières de la journée. Une sortie sportive et dépaysante accessible aux débutants.',
        'Golfe du Morbihan, 56000 Vannes', '2026-09-05 18:30:00', '2026-09-05 21:00:00',
        15.00, 25.00, 20, 'FINISHED', 'SPORT', 3, 1);

-- 17) À venir, publié, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (17, 'Initiation au tir à l’arc',
        'Apprenez à manier l’arc et à viser avec précision lors d’une séance encadrée en pleine nature. Plusieurs défis permettront de tester votre concentration et votre adresse.',
        'Parc de loisirs de la forêt, 77300 Fontainebleau', '2026-10-24 14:00:00', '2026-10-24 17:00:00',
        10.00, 18.00, 15, 'PUBLISHED', 'SPORT', 3, 1);

-- 18) À venir, publié, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (18, 'Soirée lecture en librairie',
        'Installez-vous dans une librairie chaleureuse pour découvrir des textes lus à voix haute et échanger autour de la littérature dans une ambiance intimiste.',
        'Librairie Les Pages Vives, 75005 Paris', '2026-10-29 19:00:00', '2026-10-29 21:30:00',
        0.00, 3.00, 30, 'PUBLISHED', 'CULTURE', 3, 2);

-- 19) Terminé, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (19, 'Festival de street art',
        'Assistez à la création en direct de fresques monumentales par plusieurs artistes urbains et découvrez différentes techniques de peinture et de graffiti.',
        'Bassins à flot, 33300 Bordeaux', '2026-09-26 11:00:00', '2026-09-26 20:00:00',
        0.00, 5.00, 300, 'FINISHED', 'CULTURE', 3, 2);

-- 20) À venir, publié, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (20, 'Visite théâtralisée du château',
        'Explorez un château autrement grâce à une visite menée par un comédien en costume qui vous fera revivre histoires, anecdotes et personnages du passé.',
        'Cour d''honneur du château, 37400 Amboise', '2026-11-01 15:00:00', '2026-11-01 17:00:00',
        6.00, 12.00, 40, 'PUBLISHED', 'CULTURE', 3, 2);

-- 21) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (21, 'Atelier cuisine du monde',
        'Préparez plusieurs recettes colorées inspirées de différentes cuisines du monde avant de partager vos créations autour d’un repas convivial.',
        'Cuisine collective Les Tabliers, 34000 Montpellier', '2026-11-14 10:30:00', '2026-11-14 14:30:00',
        25.00, 35.00, 14, 'PUBLISHED', 'LEISURE', 3, 1);

-- 22) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (22, 'Soirée karaoké rétro',
        'Prenez le micro seul ou entre amis et revisitez les grands classiques lors d’une soirée karaoké festive à l’ambiance rétro et décontractée.',
        'Le Disco Lounge, 31000 Toulouse', '2026-12-12 21:00:00', '2026-12-13 01:00:00',
        5.00, 10.00, 50, 'PUBLISHED', 'LEISURE', 3, 1);

-- 23) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (23, 'Atelier création de terrarium',
        'Composez votre propre jardin miniature sous verre en apprenant à choisir et disposer plantes, mousses, pierres et éléments décoratifs.',
        'Atelier Vert et Mousse, 44000 Nantes', '2026-11-28 14:00:00', '2026-11-28 16:30:00',
        22.00, 32.00, 12, 'PUBLISHED', 'LEISURE', 3, 1);

-- 24) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (24, 'Nuit d’observation des étoiles',
        'Éloignez-vous des lumières de la ville pour observer le ciel à l’aide de télescopes et découvrir constellations, planètes et objets célestes avec un animateur.',
        'Puy de Dôme, 63870 Orcines', '2026-11-14 20:30:00', '2026-11-15 00:30:00',
        6.00, 12.00, 25, 'PUBLISHED', 'LEISURE', 3, 1);

-- 25) À venir, publié, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (25, 'Initiation à l’aviron au lever du soleil',
        'Découvrez les bases de l’aviron lors d’une sortie matinale sur une eau calme. Apprenez à coordonner vos mouvements en équipe tout en profitant des premières lumières de la journée.',
        'Base nautique de la Maine, 49000 Angers', '2026-10-17 08:00:00', '2026-10-17 10:00:00',
        8.00, 14.00, 16, 'PUBLISHED', 'SPORT', 3, 1);

-- 26) Terminé, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (26, 'Tournoi de beach-volley',
        'Formez votre équipe et affrontez d’autres participants lors d’un tournoi convivial de beach-volley au bord de la mer. Une journée sportive entre sable, soleil et esprit d’équipe.',
        'Plage de Palavas-les-Flots, 34250 Palavas-les-Flots', '2026-08-22 10:00:00', '2026-08-22 18:00:00',
        10.00, 20.00, 48, 'FINISHED', 'SPORT', 3, 1);

-- 27) À venir, publié, sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (27, 'Initiation au parkour urbain',
        'Apprenez à franchir les obstacles de la ville grâce aux techniques du parkour. Un encadrement progressif permet de travailler équilibre, agilité et confiance en soi.',
        'Esplanade du parc urbain, 59000 Lille', '2026-10-31 14:00:00', '2026-10-31 17:00:00',
        6.00, 12.00, 20, 'PUBLISHED', 'SPORT', 3, 1);

-- 28) À venir, publié, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (28, 'Atelier de calligraphie japonaise',
        'Découvrez les gestes et techniques traditionnels de la calligraphie japonaise. Apprenez à manier le pinceau et l’encre pour réaliser vos premiers caractères dans une ambiance calme et créative.',
        'Atelier-salon de thé Hana, 67000 Strasbourg', '2026-11-08 15:00:00', '2026-11-08 17:30:00',
        18.00, 28.00, 12, 'PUBLISHED', 'CULTURE', 3, 2);

-- 29) À venir, publié, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (29, 'Balade photographique dans le vieux village',
        'Explorez les ruelles et l’architecture d’un village historique accompagné d’un photographe qui vous donnera des conseils sur le cadrage, la lumière et la composition.',
        'Vieux village de Ménerbes, 84560 Ménerbes', '2026-11-07 09:30:00', '2026-11-07 12:30:00',
        12.00, 20.00, 14, 'PUBLISHED', 'CULTURE', 3, 2);

-- 30) Terminé, culturel.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (30, 'Fête des lanternes au bord de l’eau',
        'Participez à une soirée lumineuse inspirée des fêtes traditionnelles, avec lanternes flottantes, animations et découverte de différentes coutumes autour de la lumière.',
        'Quais de la Saône, 71100 Chalon-sur-Saône', '2026-09-19 19:30:00', '2026-09-19 23:00:00',
        0.00, 5.00, 200, 'FINISHED', 'CULTURE', 3, 2);

-- 31) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (31, 'Atelier de création de parfum',
        'Découvrez les différentes familles olfactives et composez votre propre fragrance en associant fleurs, agrumes, épices et essences naturelles.',
        'Maison des essences, 06130 Grasse', '2026-12-05 14:00:00', '2026-12-05 17:30:00',
        35.00, 55.00, 12, 'PUBLISHED', 'LEISURE', 3, 1);

-- 32) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (32, 'Découverte de l’apiculture',
        'Enfilez une tenue d’apiculteur et découvrez le fonctionnement d’une ruche, le rôle des abeilles et les différentes étapes de fabrication du miel aux côtés d’un professionnel.',
        'Rucher pédagogique, 12000 Rodez', '2027-04-17 10:00:00', '2027-04-17 13:00:00',
        15.00, 25.00, 12, 'PUBLISHED', 'LEISURE', 3, 1);

-- 33) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (33, 'Atelier de fabrication de bougies parfumées',
        'Apprenez à fabriquer votre propre bougie en choisissant cire, parfum et éléments décoratifs. Vous repartirez avec une création personnalisée réalisée pendant l’atelier.',
        'Atelier Cire et Lumière, 21000 Dijon', '2026-12-13 14:00:00', '2026-12-13 17:00:00',
        25.00, 38.00, 14, 'PUBLISHED', 'LEISURE', 3, 1);

-- 34) À venir, publié, loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (34, 'Grande soirée quiz pop culture',
        'Constituez votre équipe et testez vos connaissances autour du cinéma, des séries, de la musique et de la culture populaire lors d’une soirée quiz pleine de défis et de bonne humeur.',
        'Brasserie Le Studio, 33000 Bordeaux', '2026-11-19 20:00:00', '2026-11-19 23:30:00',
        4.00, 8.00, 80, 'PUBLISHED', 'LEISURE', 3, 1);


-- --- Évènements de démo : états manquants (seed enrichi) ---
-- Brouillons, annulés, complet avec liste d'attente, sans date de fin, publié mais passé (à
-- terminer), et évènements d'autres organisateurs (test des droits de propriété). Ils n'ont
-- volontairement pas d'image : cela représente aussi l'état « évènement sans photo ».

-- 35) BROUILLON (visible uniquement de son organisateur), club culture.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (35, 'Rencontre avec un auteur local',
        'Une soirée d’échange avec un auteur de la région autour de son dernier roman, suivie d’une séance de dédicaces. Évènement encore en préparation.',
        'Médiathèque José Cabanis, 31000 Toulouse', '2027-01-23 18:30:00', '2027-01-23 20:30:00',
        0.00, 4.00, 40, 'DRAFT', 'CULTURE', 3, 2);

-- 36) BROUILLON d’un autre organisateur (5), club de loisirs.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (36, 'Tournoi de mölkky entre amis',
        'Un tournoi décontracté de mölkky par équipes de deux, au parc, avec buvette. Brouillon à finaliser avant publication.',
        'Parc de la Tête d’Or, 69006 Lyon', '2027-02-06 14:00:00', '2027-02-06 18:00:00',
        2.00, 5.00, 24, 'DRAFT', 'LEISURE', 5, 3);

-- 37) ANNULÉ, avec inscrits (conservé en base), club sportif.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (37, 'Course d’orientation en forêt',
        'Course d’orientation par équipes dans la forêt de Bouconne, avec cartes et balises. Évènement annulé par l’organisateur : les inscrits ont été prévenus par email.',
        'Forêt de Bouconne, 31530 Montaigut-sur-Save', '2026-11-22 09:00:00', '2026-11-22 13:00:00',
        3.00, 6.00, 30, 'CANCELLED', 'SPORT', 3, 1);

-- 38) COMPLET : 4 places, 4 inscrits et 2 personnes en liste d’attente.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (38, 'Dégustation de fromages et vins',
        'Une soirée de découverte des fromages et vins de la région, guidée par un caviste. Places très limitées : l’évènement est complet, une liste d’attente est ouverte.',
        'Cave Les Trois Sources, 69002 Lyon', '2026-11-27 19:00:00', '2026-11-27 22:00:00',
        20.00, 30.00, 4, 'PUBLISHED', 'LEISURE', 5, 3);

-- 39) Gratuit, SANS date de fin, organisé par un 3e organisateur (6), club sportif breton.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (39, 'Randonnée urbaine nocturne',
        'Une balade à pied de quartier en quartier à la tombée de la nuit, à la découverte des lieux insolites de la ville. Gratuit, sans heure de fin précise.',
        'Parvis de la gare, 35000 Rennes', '2026-11-29 18:00:00', NULL,
        0.00, 0.00, 30, 'PUBLISHED', 'SPORT', 6, 4);

-- 40) PUBLIÉ mais déjà passé : l’organisateur doit encore le « terminer ».
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (40, 'Vernissage de rentrée',
        'Vernissage de la nouvelle saison culturelle en présence des artistes. Évènement passé mais pas encore clôturé par son organisateur.',
        'Espace des Arts, 31000 Toulouse', '2026-10-03 18:00:00', '2026-10-03 21:00:00',
        0.00, 0.00, 40, 'PUBLISHED', 'CULTURE', 3, 2);

-- 41) ANNULÉ à la suppression du club (5, désaffilié) : l’organisateur (17) est redevenu simple membre.
INSERT INTO events (id, title, description, location, start_date_time, end_date_time,
                     affiliated_price, non_affiliated_price, max_seats, status, category,
                     organizer_id, club_id)
VALUES (41, 'Soirée jeu de piste urbain',
        'Un jeu de piste en équipes dans les rues de la ville. Évènement annulé suite à la fin d’affiliation du club organisateur.',
        'Centre-ville, 38000 Grenoble', '2026-11-05 18:30:00', '2026-11-05 21:30:00',
        4.00, 8.00, 25, 'CANCELLED', 'SPORT', 17, 5);


-- --- Images de démo (EVT-10) ---
-- Hébergées sur imgbb (voir ImgbbClient), une par évènement, choisies pour être cohérentes
-- avec le thème de chacun (ex. photo de concert sur l'évènement concert).
-- Chaque évènement a une image de prévisualisation (is_preview = TRUE) : celle affichée sur sa carte.
INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (1, 1, 'https://i.ibb.co/qMVP6r11/event-football.webp',
        'https://ibb.co/tMFWRJCC/19bc3f3f7e3ad54be2a12e4ca068c3e5', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (2, 2, 'https://i.ibb.co/Q3DXfRZv/event-museum.webp',
        'https://ibb.co/9kZGNLJH/8fba30329be5b426345eb9d58d9b5d4c', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (3, 3, 'https://i.ibb.co/nqyvgH7t/event-music.webp',
        'https://ibb.co/8nwpKWrt/e2e8ae06afd85017e2e2d17cf2282dc3', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (4, 4, 'https://i.ibb.co/350pp0nM/event-boardgame.webp',
        'https://ibb.co/CpnbbnfM/22a4003119d5be178cccad2ccbc2e5e1', TRUE);

-- Photos des 30 évènements supplémentaires (EVT-13) : hébergées sur imgbb, une photo de preview par évènement.
INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (5, 5, 'https://i.ibb.co/67jv0g8J/loisir-soiree-jeux-cafe-ludique.webp',
        'https://ibb.co/fzj8d49G/a045794dc98d057fc5861594b711aa60', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (6, 6, 'https://i.ibb.co/wNcFwmdV/culture-cinema-plein-air.webp',
        'https://ibb.co/4Z1nYrFz/fa834e3d5d26a19c95ebd049ec0393a0', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (7, 7, 'https://i.ibb.co/1Yc1C6PR/culture-exposition-art-contemporain.webp',
        'https://ibb.co/NgR50SJW/72cd15a8ff9bcaad87deaab91b1edaf6', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (8, 8, 'https://i.ibb.co/rRV4LkPh/culture-concert-jazz-bord-du-lac.webp',
        'https://ibb.co/Q34rq9BV/90b8115272dd6e740a8ff9f467e2a67e', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (9, 9, 'https://i.ibb.co/C5vmNcSV/sport-marathon-centre-ville.webp',
        'https://ibb.co/vxmVgFML/36ec7aa16f10552f468245a6db085abc', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (10, 10, 'https://i.ibb.co/7d0dwNhv/sport-tournoi-futsal-nocturne.webp',
        'https://ibb.co/mVPVLFpJ/909d3522bc779b8891e055c45eabb020', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (11, 11, 'https://i.ibb.co/p6FV4mnw/sport-yoga-lever-du-soleil.webp',
        'https://ibb.co/xKT9z43L/227b5f186551da832ded99a8e145ce58', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (12, 12, 'https://i.ibb.co/Fkk73Y1H/loisir-escape-game-mystere-egyptien.webp',
        'https://ibb.co/7xxyCpDk/323c9e9453a8f0fa8ef8bddaf39fa201', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (13, 13, 'https://i.ibb.co/YF6V1Dvp/loisir-brunch-fleuri-jardin.webp',
        'https://ibb.co/mVmP2vQ6/158fdc7eff392b5201c8138402b12ccc', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (14, 14, 'https://i.ibb.co/V0vnDy8p/loisir-atelier-poterie.webp',
        'https://ibb.co/5X5pvDVx/4e598e049511e6b7907698248eff560a', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (15, 15, 'https://i.ibb.co/zVts4qdt/sport-initiation-escalade-bloc.webp',
        'https://ibb.co/YBV2LHgV/a60bb9cf81ce7d2392e1438e0772b65b', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (16, 16, 'https://i.ibb.co/mrhbJkDL/sport-paddle-coucher-du-soleil.webp',
        'https://ibb.co/CpVmBg1k/b51bb126f61d97b2ab69a4866c3cdc3b', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (17, 17, 'https://i.ibb.co/N60vnkyk/sport-initiation-tir-a-l-arc.webp',
        'https://ibb.co/mCnjFYcY/2bc3eea73fd31ebbc514fcb3c257a690', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (18, 18, 'https://i.ibb.co/RwnZTzR/culture-soiree-lecture-librairie.webp',
        'https://ibb.co/V4yrY2f/f8a488fea842e270dd2720ed26578dd2', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (19, 19, 'https://i.ibb.co/bM2wM341/culture-festival-street-art.webp',
        'https://ibb.co/9kVKkvxn/3a6a41d8b498b32bc423d6ade184111f', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (20, 20, 'https://i.ibb.co/4nPct1rZ/culture-visite-theatralisee-chateau.webp',
        'https://ibb.co/q3kzjJQM/a83650961f1298b450d5abf7b31ce0dd', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (21, 21, 'https://i.ibb.co/S4JmXjxY/loisir-atelier-cuisine-du-monde.webp',
        'https://ibb.co/G4FMv6JY/42b5a6d231a3fb107bd4bbec254faee1', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (22, 22, 'https://i.ibb.co/N63fQNxN/loisir-soiree-karaoke-retro.webp',
        'https://ibb.co/0jX189q9/4c293b54d3248ae4c7fd1cc6610f648a', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (23, 23, 'https://i.ibb.co/L3x803w/loisir-atelier-creation-terrarium.webp',
        'https://ibb.co/jSyT8SK/39b29adde4e43a42ca623f050957481e', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (24, 24, 'https://i.ibb.co/S4jzFTtz/loisir-nuit-observation-etoiles.webp',
        'https://ibb.co/5g0PbDrP/2e209225f507a01d145fc445843827c2', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (25, 25, 'https://i.ibb.co/KptGq4ML/sport-initiation-aviron-lever-du-soleil.webp',
        'https://ibb.co/Xrn4s0h3/9d48cdaf22b6afed24c91ce9c2e5bfe6', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (26, 26, 'https://i.ibb.co/8L2GbxTg/sport-tournoi-beach-volley.webp',
        'https://ibb.co/TxRJ10pM/d8919fa2142854d03c03224da21beddd', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (27, 27, 'https://i.ibb.co/9kLkBQq6/sport-initiation-parkour-urbain.webp',
        'https://ibb.co/7d5dBsGP/286bb3a9902b34232df0994f23ed9084', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (28, 28, 'https://i.ibb.co/YFX4jyGw/culture-atelier-calligraphie-japonaise.webp',
        'https://ibb.co/mV0rzX7g/4464d42e49a460023f27ebb853b2716f', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (29, 29, 'https://i.ibb.co/8gr90rw9/culture-balade-photographique-vieux-village.webp',
        'https://ibb.co/xq16D1b6/75614296211c3b165722689c2c9acd1d', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (30, 30, 'https://i.ibb.co/RfbYFS1/culture-fete-lanternes-bord-de-l-eau.webp',
        'https://ibb.co/LmRYFPM/93478aae49851eaf3546f024dd1c97f8', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (31, 31, 'https://i.ibb.co/5NhqJH8/loisir-atelier-creation-parfum.webp',
        'https://ibb.co/9FHRd5T/42c4a749ee4d2d9a42b5dca0a7583c47', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (32, 32, 'https://i.ibb.co/VcymCYb0/loisir-decouverte-apiculture.webp',
        'https://ibb.co/bMhJv5Yj/0c47394d4ec0952c5c564ac9e3596477', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (33, 33, 'https://i.ibb.co/W4mW2g4k/loisir-atelier-bougies-parfumees.webp',
        'https://ibb.co/ksNgSQs4/c009f9dd74345e07c27fdc15e5698642', TRUE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (34, 34, 'https://i.ibb.co/b5JY8yXp/loisir-soiree-quiz-pop-culture.webp',
        'https://ibb.co/xqHwRc6V/f29ee744efeadd4569bf3dd2e00fc8d6', TRUE);


-- Photos supplémentaires (EVT-13) : une 2e photo (is_preview = FALSE) pour 3 évènements, pour montrer la galerie
-- de la page de détail (preview en premier, puis les autres photos par ordre d'ajout).
INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (35, 5, 'https://i.ibb.co/6cpVqQC2/loisir-soiree-jeux-cafe-ludique-2.webp',
        'https://ibb.co/kVrkPZCp/731b752b6a04afcd55712a507bb96de0', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (36, 7, 'https://i.ibb.co/VWxVHWcG/culture-exposition-art-contemporain-2.webp',
        'https://ibb.co/TB1cKBxG/6bcbc9902e54b6f1a3c9b8c8c8385eee', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (37, 10, 'https://i.ibb.co/RpXQbfrh/sport-tournoi-futsal-nocturne-2.webp',
        'https://ibb.co/1fCRQ1ys/f13b2770f663e76fc1e410415c1590bf', FALSE);

-- --- Commentaires de démo (COM-02) ---
-- Auteurs limités aux comptes ACTIFS (règle CU12 : compte actif requis) : le membre en
-- attente d'activation (id 4) n'en publie donc aucun.

-- Sur le tournoi de foot à 5 (à venir)
INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (1, 'Super initiative ! Je m''inscris avec grand plaisir, ça fait longtemps que je n''ai pas tapé dans un ballon :)',
        1, 1, '2026-09-10 14:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (2, 'Belle initiative de l''équipe d''organisation. N''hésitez pas si vous avez besoin de bénévoles pour l''accueil.',
        2, 1, '2026-09-11 09:30:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (3, 'Merci à tous pour votre enthousiasme ! Pensez à prévoir une bouteille d''eau, la salle peut vite chauffer.',
        3, 1, '2026-09-12 17:45:00');

-- Sur l'exposition d'art contemporain (terminée)
INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (4, 'Très belle exposition, les sculptures dialoguent vraiment bien avec les toiles abstraites.',
        1, 2, '2026-08-11 10:15:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (5, 'Dommage d''avoir manqué le vernissage, est-ce qu''une captation vidéo sera mise en ligne ?',
        2, 2, '2026-08-11 19:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (6, 'Merci à toutes les personnes venues nombreuses ! On prépare déjà la suite pour l''année prochaine.',
        3, 2, '2026-08-12 08:00:00');

-- --- Inscriptions de démo ---
-- REGISTERED = inscrit, WAITING_LIST = liste d'attente (évènement complet). Aucun chevauchement
-- d'horaires par utilisateur (règle CU9). Pour tester ce blocage : le membre 1 est inscrit à l'évènement 4
-- (14/11, 19h30) : s'inscrire à l'évènement 24 (14/11, 20h30) doit être refusé.

-- Évènements terminés
-- 2 · Exposition regards sur la ville
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (1, 1, 2, 'REGISTERED', '2026-07-31 11:07:00'),
  (2, 8, 2, 'REGISTERED', '2026-07-31 13:56:00'),
  (3, 9, 2, 'REGISTERED', '2026-07-31 18:03:00'),
  (4, 16, 2, 'REGISTERED', '2026-07-31 20:52:00');
-- 26 · Beach-volley
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (5, 7, 26, 'REGISTERED', '2026-08-12 13:49:00'),
  (6, 10, 26, 'REGISTERED', '2026-08-12 16:10:00'),
  (7, 12, 26, 'REGISTERED', '2026-08-12 17:24:00'),
  (8, 15, 26, 'REGISTERED', '2026-08-12 23:45:00');
-- 6 · Cinéma en plein air
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (9, 1, 6, 'REGISTERED', '2026-08-19 22:07:00'),
  (10, 7, 6, 'REGISTERED', '2026-08-20 03:49:00'),
  (11, 8, 6, 'REGISTERED', '2026-08-20 03:56:00'),
  (12, 9, 6, 'REGISTERED', '2026-08-20 08:03:00'),
  (13, 10, 6, 'REGISTERED', '2026-08-20 12:10:00'),
  (14, 16, 6, 'REGISTERED', '2026-08-20 13:52:00');
-- 16 · Paddle
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (15, 7, 16, 'REGISTERED', '2026-08-26 22:19:00'),
  (16, 12, 16, 'REGISTERED', '2026-08-26 22:54:00'),
  (17, 9, 16, 'REGISTERED', '2026-08-27 02:33:00');
-- 8 · Concert de jazz
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (18, 1, 8, 'REGISTERED', '2026-09-02 20:07:00'),
  (19, 8, 8, 'REGISTERED', '2026-09-02 22:56:00'),
  (20, 9, 8, 'REGISTERED', '2026-09-03 03:03:00'),
  (21, 11, 8, 'REGISTERED', '2026-09-03 08:17:00'),
  (22, 16, 8, 'REGISTERED', '2026-09-03 08:52:00');
-- 13 · Brunch fleuri
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (23, 7, 13, 'REGISTERED', '2026-09-03 14:19:00'),
  (24, 9, 13, 'REGISTERED', '2026-09-03 15:33:00'),
  (25, 10, 13, 'REGISTERED', '2026-09-03 19:40:00');
-- 30 · Lanternes
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (26, 1, 30, 'REGISTERED', '2026-09-09 20:37:00'),
  (27, 7, 30, 'REGISTERED', '2026-09-10 02:19:00'),
  (28, 8, 30, 'REGISTERED', '2026-09-10 02:26:00'),
  (29, 11, 30, 'REGISTERED', '2026-09-10 08:47:00'),
  (30, 16, 30, 'REGISTERED', '2026-09-10 09:22:00');
-- 19 · Festival de street art
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (31, 1, 19, 'REGISTERED', '2026-09-16 12:07:00'),
  (32, 7, 19, 'REGISTERED', '2026-09-16 17:49:00'),
  (33, 8, 19, 'REGISTERED', '2026-09-16 17:56:00'),
  (34, 9, 19, 'REGISTERED', '2026-09-16 22:03:00'),
  (35, 10, 19, 'REGISTERED', '2026-09-17 02:10:00'),
  (36, 11, 19, 'REGISTERED', '2026-09-17 06:17:00'),
  (37, 12, 19, 'REGISTERED', '2026-09-17 06:24:00'),
  (38, 13, 19, 'REGISTERED', '2026-09-17 10:31:00'),
  (39, 14, 19, 'REGISTERED', '2026-09-17 14:38:00'),
  (40, 15, 19, 'REGISTERED', '2026-09-17 18:45:00'),
  (41, 16, 19, 'REGISTERED', '2026-09-17 18:52:00');

-- Évènement publié mais passé (à terminer)
-- 40 · Vernissage (passé, à terminer)
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (42, 1, 40, 'REGISTERED', '2026-09-23 19:07:00'),
  (43, 7, 40, 'REGISTERED', '2026-09-24 00:49:00'),
  (44, 8, 40, 'REGISTERED', '2026-09-24 00:56:00'),
  (45, 9, 40, 'REGISTERED', '2026-09-24 05:03:00');

-- Évènements à venir
-- 11 · Yoga
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (46, 1, 11, 'REGISTERED', '2026-10-01 08:52:00'),
  (47, 7, 11, 'REGISTERED', '2026-10-01 14:34:00'),
  (48, 9, 11, 'REGISTERED', '2026-10-01 15:48:00');
-- 5 · Café ludique (Lyon)
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (49, 1, 5, 'REGISTERED', '2026-10-02 13:07:00'),
  (50, 7, 5, 'REGISTERED', '2026-10-02 18:49:00'),
  (51, 9, 5, 'REGISTERED', '2026-10-02 20:03:00');
-- 25 · Aviron
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (52, 8, 25, 'REGISTERED', '2026-10-06 12:56:00'),
  (53, 12, 25, 'REGISTERED', '2026-10-06 16:24:00');
-- 15 · Escalade en bloc
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (54, 7, 15, 'REGISTERED', '2026-10-04 15:49:00'),
  (55, 10, 15, 'REGISTERED', '2026-10-04 18:10:00'),
  (56, 12, 15, 'REGISTERED', '2026-10-04 19:24:00');
-- 3 · Concert acoustique
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (57, 1, 3, 'REGISTERED', '2026-10-04 13:07:00'),
  (58, 7, 3, 'REGISTERED', '2026-10-04 18:49:00'),
  (59, 8, 3, 'REGISTERED', '2026-10-04 18:56:00'),
  (60, 9, 3, 'REGISTERED', '2026-10-04 23:03:00'),
  (61, 10, 3, 'REGISTERED', '2026-10-05 03:10:00'),
  (62, 11, 3, 'REGISTERED', '2026-10-05 07:17:00'),
  (63, 12, 3, 'REGISTERED', '2026-10-05 07:24:00'),
  (64, 15, 3, 'REGISTERED', '2026-10-05 13:45:00'),
  (65, 6, 3, 'REGISTERED', '2026-10-05 14:42:00');
-- 10 · Futsal nocturne
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (66, 7, 10, 'REGISTERED', '2026-10-03 15:49:00'),
  (67, 12, 10, 'REGISTERED', '2026-10-03 16:24:00'),
  (68, 10, 10, 'REGISTERED', '2026-10-03 21:10:00'),
  (69, 8, 10, 'REGISTERED', '2026-10-03 21:56:00');
-- 17 · Tir à l’arc
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (70, 1, 17, 'REGISTERED', '2026-10-02 13:07:00'),
  (71, 8, 17, 'REGISTERED', '2026-10-02 15:56:00'),
  (72, 9, 17, 'REGISTERED', '2026-10-02 20:03:00');
-- 9 · Marathon
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (73, 7, 9, 'REGISTERED', '2026-10-04 15:49:00'),
  (74, 10, 9, 'REGISTERED', '2026-10-04 18:10:00'),
  (75, 11, 9, 'REGISTERED', '2026-10-04 22:17:00'),
  (76, 15, 9, 'REGISTERED', '2026-10-05 01:45:00'),
  (77, 12, 9, 'REGISTERED', '2026-10-05 01:24:00'),
  (78, 8, 9, 'REGISTERED', '2026-10-05 03:56:00'),
  (79, 6, 9, 'REGISTERED', '2026-10-05 08:42:00');
-- 18 · Soirée lecture
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (80, 8, 18, 'REGISTERED', '2026-10-07 12:56:00'),
  (81, 1, 18, 'REGISTERED', '2026-10-07 16:07:00'),
  (82, 11, 18, 'REGISTERED', '2026-10-07 22:17:00');
-- 27 · Parkour
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (83, 7, 27, 'REGISTERED', '2026-10-04 15:49:00'),
  (84, 10, 27, 'REGISTERED', '2026-10-04 18:10:00');
-- 20 · Visite du château
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (85, 8, 20, 'REGISTERED', '2026-10-05 12:56:00'),
  (86, 9, 20, 'REGISTERED', '2026-10-05 17:03:00'),
  (87, 1, 20, 'REGISTERED', '2026-10-05 19:07:00');
-- 29 · Balade photo
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (88, 1, 29, 'REGISTERED', '2026-10-02 13:07:00'),
  (89, 8, 29, 'REGISTERED', '2026-10-02 15:56:00'),
  (90, 11, 29, 'REGISTERED', '2026-10-02 22:17:00');
-- 12 · Escape game
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (91, 1, 12, 'REGISTERED', '2026-10-07 13:07:00'),
  (92, 7, 12, 'REGISTERED', '2026-10-07 18:49:00'),
  (93, 9, 12, 'REGISTERED', '2026-10-07 20:03:00'),
  (94, 10, 12, 'REGISTERED', '2026-10-08 00:10:00');
-- 28 · Calligraphie
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (95, 8, 28, 'REGISTERED', '2026-10-03 12:56:00'),
  (96, 11, 28, 'REGISTERED', '2026-10-03 19:17:00'),
  (97, 9, 28, 'REGISTERED', '2026-10-03 20:03:00');
-- 7 · Exposition (Nantes)
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (98, 1, 7, 'REGISTERED', '2026-10-06 13:07:00'),
  (99, 8, 7, 'REGISTERED', '2026-10-06 15:56:00'),
  (100, 9, 7, 'REGISTERED', '2026-10-06 20:03:00'),
  (101, 11, 7, 'REGISTERED', '2026-10-07 01:17:00');
-- 21 · Cuisine du monde
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (102, 9, 21, 'REGISTERED', '2026-10-04 14:03:00'),
  (103, 10, 21, 'REGISTERED', '2026-10-04 18:10:00');
-- 4 · Soirée jeux de société
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (104, 1, 4, 'REGISTERED', '2026-10-03 13:07:00'),
  (105, 7, 4, 'REGISTERED', '2026-10-03 18:49:00'),
  (106, 10, 4, 'REGISTERED', '2026-10-03 21:10:00');
-- 24 · Étoiles
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (107, 8, 24, 'REGISTERED', '2026-10-07 12:56:00'),
  (108, 9, 24, 'REGISTERED', '2026-10-07 17:03:00'),
  (109, 11, 24, 'REGISTERED', '2026-10-07 22:17:00'),
  (110, 12, 24, 'REGISTERED', '2026-10-07 22:24:00');
-- 34 · Quiz pop culture
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (111, 1, 34, 'REGISTERED', '2026-10-03 13:07:00'),
  (112, 7, 34, 'REGISTERED', '2026-10-03 18:49:00'),
  (113, 8, 34, 'REGISTERED', '2026-10-03 18:56:00'),
  (114, 9, 34, 'REGISTERED', '2026-10-03 23:03:00'),
  (115, 10, 34, 'REGISTERED', '2026-10-04 03:10:00'),
  (116, 11, 34, 'REGISTERED', '2026-10-04 07:17:00'),
  (117, 12, 34, 'REGISTERED', '2026-10-04 07:24:00');
-- 14 · Poterie
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (118, 7, 14, 'REGISTERED', '2026-10-05 15:49:00'),
  (119, 9, 14, 'REGISTERED', '2026-10-05 17:03:00'),
  (120, 1, 14, 'REGISTERED', '2026-10-05 19:07:00');
-- 38 · Dégustation (complet)
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (121, 7, 38, 'REGISTERED', '2026-10-05 15:49:00'),
  (122, 8, 38, 'REGISTERED', '2026-10-05 15:56:00'),
  (123, 9, 38, 'REGISTERED', '2026-10-05 20:03:00'),
  (124, 10, 38, 'REGISTERED', '2026-10-06 00:10:00'),
  (125, 11, 38, 'WAITING_LIST', '2026-10-06 04:17:00'),
  (126, 12, 38, 'WAITING_LIST', '2026-10-06 04:24:00');
-- 23 · Terrarium
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (127, 8, 23, 'REGISTERED', '2026-10-02 12:56:00'),
  (128, 10, 23, 'REGISTERED', '2026-10-02 18:10:00');
-- 39 · Randonnée urbaine
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (129, 7, 39, 'REGISTERED', '2026-10-04 15:49:00'),
  (130, 9, 39, 'REGISTERED', '2026-10-04 17:03:00'),
  (131, 12, 39, 'REGISTERED', '2026-10-04 19:24:00'),
  (132, 8, 39, 'REGISTERED', '2026-10-04 21:56:00');
-- 31 · Parfum
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (133, 1, 31, 'REGISTERED', '2026-10-06 13:07:00'),
  (134, 8, 31, 'REGISTERED', '2026-10-06 15:56:00'),
  (135, 9, 31, 'REGISTERED', '2026-10-06 20:03:00');
-- 1 · Tournoi de foot à 5
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (136, 1, 1, 'REGISTERED', '2026-10-06 13:07:00'),
  (137, 7, 1, 'REGISTERED', '2026-10-06 18:49:00'),
  (138, 10, 1, 'REGISTERED', '2026-10-06 21:10:00'),
  (139, 12, 1, 'REGISTERED', '2026-10-06 22:24:00'),
  (140, 15, 1, 'REGISTERED', '2026-10-07 04:45:00'),
  (141, 6, 1, 'REGISTERED', '2026-10-07 05:42:00');
-- 22 · Karaoké rétro
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (142, 1, 22, 'REGISTERED', '2026-10-03 13:07:00'),
  (143, 7, 22, 'REGISTERED', '2026-10-03 18:49:00'),
  (144, 8, 22, 'REGISTERED', '2026-10-03 18:56:00'),
  (145, 9, 22, 'REGISTERED', '2026-10-03 23:03:00'),
  (146, 11, 22, 'REGISTERED', '2026-10-04 04:17:00'),
  (147, 12, 22, 'REGISTERED', '2026-10-04 04:24:00');
-- 33 · Bougies
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (148, 8, 33, 'REGISTERED', '2026-10-04 12:56:00'),
  (149, 9, 33, 'REGISTERED', '2026-10-04 17:03:00'),
  (150, 11, 33, 'REGISTERED', '2026-10-04 22:17:00');
-- 32 · Apiculture
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (151, 1, 32, 'REGISTERED', '2026-10-05 13:07:00'),
  (152, 9, 32, 'REGISTERED', '2026-10-05 17:03:00'),
  (153, 10, 32, 'REGISTERED', '2026-10-05 21:10:00');

-- Évènements annulés (inscrits conservés)
-- 37 · Course d’orientation (annulé)
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (154, 7, 37, 'REGISTERED', '2026-10-06 15:49:00'),
  (155, 8, 37, 'REGISTERED', '2026-10-06 15:56:00'),
  (156, 12, 37, 'REGISTERED', '2026-10-06 19:24:00');
-- 41 · Jeu de piste (club supprimé)
INSERT INTO registrations (id, user_id, event_id, status, registration_date) VALUES
  (157, 8, 41, 'REGISTERED', '2026-10-02 12:56:00'),
  (158, 10, 41, 'REGISTERED', '2026-10-02 18:10:00');

-- --- Commentaires de démo supplémentaires ---
-- Variés : questions/réponses de l'organisateur, commentaire très long (mise en page), sur évènement
-- terminé, annulé et complet, et commentaires d'un compte ANONYMISÉ (16) affichés « SUPPRIMÉ ».

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (7, 'Quelqu’un sait s’il y aura un parking près de l’entrée ? On viendra à plusieurs voitures.',
        7, 3, '2026-10-01 18:20:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (8, 'Bonjour, un parking gratuit est disponible à 200 m de l’entrée principale. Venez un peu en avance !',
        3, 3, '2026-10-02 09:05:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (9, 'Hâte d’y être, j’ai déjà prévu la couverture et le thermos.',
        9, 3, '2026-10-03 20:40:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (10, 'Y a-t-il un vestiaire avec douches sur place ? Merci d’avance.',
        12, 1, '2026-09-14 20:10:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (11, 'Superbe festival ! Les fresques en cours de réalisation sont impressionnantes, bravo aux artistes.',
        1, 19, '2026-09-27 10:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (12, 'Une journée magnifique, on a pu discuter avec plusieurs artistes. À refaire !',
        16, 19, '2026-09-27 15:30:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (13, 'Franchement, je ne m’attendais pas à une telle ampleur. Les murs des anciens bassins se sont transformés en véritable galerie à ciel ouvert, avec des artistes venus de toute la France qui expliquaient leurs techniques au public. J’ai particulièrement aimé la démonstration au pochoir et les ateliers pour les enfants. L’organisation était impeccable, avec de l’ombre, de l’eau à disposition et des bénévoles très accueillants. Un grand merci, je serai présent l’an prochain avec toute ma famille.',
        9, 19, '2026-09-28 08:15:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (14, 'Un moment suspendu au bord du lac, le quartet était exceptionnel.',
        8, 8, '2026-09-13 09:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (15, 'Merci pour cette belle soirée.',
        16, 8, '2026-09-13 11:45:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (16, 'Très bonne ambiance sous les guirlandes. Le film était bien choisi.',
        7, 6, '2026-08-30 10:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (17, 'Dommage que le son soit un peu faible depuis le fond du parc.',
        10, 6, '2026-08-30 12:15:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (18, 'Je suis en liste d’attente, je croise les doigts pour qu’une place se libère !',
        11, 38, '2026-10-07 21:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (19, 'Pas de panique : si quelqu’un se désiste, le premier de la liste est automatiquement inscrit et prévenu par mail.',
        5, 38, '2026-10-08 08:30:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (20, 'Dommage pour l’annulation, j’espère une nouvelle date bientôt.',
        7, 37, '2026-10-05 19:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (21, 'Merci pour votre compréhension. Une nouvelle date sera proposée dès que la météo le permettra.',
        3, 37, '2026-10-05 20:10:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (22, 'Belle soirée, merci à l’équipe pour l’accueil !',
        8, 40, '2026-10-04 10:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (23, 'Les sculptures en lévitation m’ont marqué. Très belle exposition.',
        16, 2, '2026-08-12 14:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (24, 'Les lanternes sur l’eau, un spectacle magique. Merci aux organisateurs !',
        11, 30, '2026-09-20 09:30:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (25, 'Quel est le dernier délai pour se désinscrire ?',
        15, 9, '2026-10-06 18:00:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (26, 'Vous pouvez vous désinscrire à tout moment depuis la page « Mon calendrier ».',
        3, 9, '2026-10-06 19:20:00');

INSERT INTO comments (id, content, user_id, event_id, created_at)
VALUES (27, 'On est quatre à vouloir tenter le mystère égyptien, ça va chauffer !',
        1, 12, '2026-10-02 12:00:00');

-- --- Demandes d'anonymisation (CU28/CU29) ---
-- 2 demandes EN ATTENTE (membre 12 et organisateur 6 : ses évènements publiés à venir seront
-- auto-annulés à la validation) et 1 demande VALIDÉE (compte 16, déjà anonymisé).
INSERT INTO anonymization_requests (id, user_id, status, request_date)
VALUES (1, 12, 'PENDING', '2026-10-06 10:15:00');

INSERT INTO anonymization_requests (id, user_id, status, request_date)
VALUES (2, 6, 'PENDING', '2026-10-07 16:40:00');

INSERT INTO anonymization_requests (id, user_id, status, request_date)
VALUES (3, 16, 'VALIDATED', '2026-09-30 09:00:00');

-- --- Documents légaux (CU31) : CGU et politique RGPD, affichés dans les pages publiques ---
INSERT INTO legal_documents (id, type, content, updated_at)
VALUES (1, 'CGU', 'Conditions Générales d’Utilisation du Hub évènementiel

Article 1 - Objet
Les présentes conditions encadrent l’utilisation de la plateforme du Hub évènementiel, qui permet aux membres d’une fédération de clubs de consulter, d’organiser et de s’inscrire à des évènements sportifs, culturels et de loisirs.

Article 2 - Création et utilisation d’un compte
La création d’un compte nécessite une adresse email valide et un mot de passe respectant les règles de sécurité en vigueur (12 caractères minimum, avec majuscule, minuscule, chiffre et caractère spécial). L’utilisateur est responsable de la confidentialité de ses identifiants.

Article 3 - Inscription aux évènements
Un utilisateur connecté peut s’inscrire à un évènement publié tant qu’il reste des places. Lorsque l’évènement est complet, il est placé sur liste d’attente et informé par email dès qu’une place se libère. Il est impossible de s’inscrire à deux évènements dont les horaires se chevauchent.

Article 4 - Tarifs
Les tarifs affichés sont donnés à titre d’information. Aucun paiement n’est effectué sur la plateforme : le règlement a lieu sur place le jour de l’évènement.

Article 5 - Comportement des utilisateurs
Les commentaires et les échanges doivent rester courtois. Tout propos injurieux, discriminatoire ou contraire à la loi peut entraîner la suspension, temporaire ou définitive, du compte par un administrateur, avec notification par email du motif.

Article 6 - Responsabilité des organisateurs
Les organisateurs sont responsables du contenu de leurs évènements et des informations publiées. Ils peuvent annuler un évènement publié comportant des inscrits : les personnes concernées en sont alors informées par email.

Article 7 - Protection des données
Les données personnelles sont traitées conformément à la politique RGPD de la plateforme. Chaque utilisateur peut demander l’anonymisation de son compte, qui est traitée par un administrateur.

Article 8 - Modification des conditions
Les présentes conditions peuvent être mises à jour à tout moment par l’administrateur de la plateforme. La date de dernière mise à jour est indiquée sur cette page.', '2026-09-01 10:00:00');

INSERT INTO legal_documents (id, type, content, updated_at)
VALUES (2, 'RGPD', 'Politique de protection des données personnelles (RGPD)

1. Responsable du traitement
La fédération de clubs éditrice du Hub évènementiel est responsable du traitement des données personnelles collectées via la plateforme.

2. Données collectées
Nom, prénom, adresse postale, adresse email, numéro de téléphone (facultatif), club(s) d’affiliation, inscriptions aux évènements et commentaires publiés.

3. Finalités
Ces données servent uniquement à gérer les comptes, les affiliations, les inscriptions aux évènements, l’envoi des emails de service (activation, confirmation, annulation, suspension) et la modération des échanges.

4. Durée de conservation
Les données sont conservées tant que le compte est actif. Après anonymisation, les informations personnelles sont remplacées par des valeurs aléatoires et irréversibles. Les commentaires publiés sont conservés mais restent anonymes, leur auteur apparaissant comme « SUPPRIMÉ ».

5. Vos droits
Vous pouvez accéder à vos données, les rectifier depuis la page « Mon compte », ou demander leur anonymisation. La demande est examinée puis validée par un administrateur.

6. Sécurité
Les mots de passe sont stockés sous forme chiffrée et ne sont jamais accessibles en clair. Les échanges avec la plateforme sont protégés.

7. Contact
Pour toute question relative à vos données, contactez l’administrateur de la plateforme à l’adresse affichée sur la page d’accueil.', '2026-09-01 10:05:00');
