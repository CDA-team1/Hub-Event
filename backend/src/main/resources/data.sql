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

-- Photos supplémentaires des 34 évènements : 82 photos hébergées sur imgbb (1 à 3 par évènement).
-- La photo de preview reste celle d'origine ci-dessus ; ces photos n'apparaissent que dans la galerie.
INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (38, 1, 'https://i.ibb.co/C3ffkr2Y/event-football-2.webp',
        'https://ibb.co/q3vvtZ5c/df5ba37ca6546c54a01da6979b59e5f7', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (39, 1, 'https://i.ibb.co/XZ6FXWKH/event-football-3.webp',
        'https://ibb.co/3yVS1TjK/799ce924132f88a88b41b0a223f58b07', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (40, 1, 'https://i.ibb.co/Rkv2LNxS/event-football-4.webp',
        'https://ibb.co/fz1Mm9hS/e35997027ea2dcb9e5e22bd50d120ae3', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (41, 2, 'https://i.ibb.co/233dt7YP/event-museum-2.webp',
        'https://ibb.co/600FPRJb/2de619491a021e3a34dab29b71f7d8af', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (42, 2, 'https://i.ibb.co/0RBwdMTS/event-museum-3.webp',
        'https://ibb.co/Rkh5V0fm/1907a503afa1409a2e274d61e5a86caf', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (43, 3, 'https://i.ibb.co/svqTzwW9/event-music-2.webp',
        'https://ibb.co/ynqz7y4F/1693d6ae9192f9e729aaa4a81357f3b7', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (44, 3, 'https://i.ibb.co/vvJqTCRW/event-music-3.webp',
        'https://ibb.co/bRrmCjDV/b54cf98945b42dcd2b00f85dee7bddb7', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (45, 3, 'https://i.ibb.co/8nwBgpNn/event-music-4.webp',
        'https://ibb.co/35bsmq45/26e46ac3c61960dc2d2c6a60158f7388', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (46, 4, 'https://i.ibb.co/bRmw8Npv/event-boardgame-2.webp',
        'https://ibb.co/Fq3Zj8GY/f57f2d5b0d9c926b1a96adb63ae61247', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (47, 4, 'https://i.ibb.co/mCN6nYMr/event-boardgame-3.webp',
        'https://ibb.co/SXfRk19D/ba40e4c5118f9e36c35a63e8c6a41ff4', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (48, 5, 'https://i.ibb.co/SX2dCcKh/loisir-soiree-jeux-cafe-ludique-3.webp',
        'https://ibb.co/Fb90TXBS/eeb97e94d8655d03152b7aaf5b299b10', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (49, 5, 'https://i.ibb.co/LXCQXkTL/loisir-soiree-jeux-cafe-ludique-4.webp',
        'https://ibb.co/4njsnmb6/5114dbde5d47cb6223484e6eb4c32e4f', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (50, 6, 'https://i.ibb.co/QZS3XnN/culture-cinema-plein-air-2.webp',
        'https://ibb.co/4cvnJMT/03061f14b150c04322a5989cb21eb759', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (51, 6, 'https://i.ibb.co/qL1TM606/culture-cinema-plein-air-3.webp',
        'https://ibb.co/20KG7J8J/bab5f69d61aa266226e9cc49f9df5b40', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (52, 7, 'https://i.ibb.co/HDwPcFcq/culture-exposition-art-contemporain-3.webp',
        'https://ibb.co/d0CpNWND/4f7c948f6f021589b58e47c40df56f4b', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (53, 7, 'https://i.ibb.co/n8c1NkDH/culture-exposition-art-contemporain-4.webp',
        'https://ibb.co/PzhWvC57/4134a7a8d3e3466635f357389778d4f1', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (54, 8, 'https://i.ibb.co/K4xFsBR/culture-concert-jazz-bord-du-lac-2.webp',
        'https://ibb.co/X0xS56N/e006544ac883f98e705ec85dd1c5e665', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (55, 8, 'https://i.ibb.co/dJfBZ4N4/culture-concert-jazz-bord-du-lac-3.webp',
        'https://ibb.co/jZ8htkCk/22ae3eadff2aeb87b064cf84670bec80', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (56, 9, 'https://i.ibb.co/sBFqmWQ/sport-marathon-centre-ville-2.webp',
        'https://ibb.co/cmNQgYt/a13aac8e8340b12cec2affa9a94a72f8', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (57, 9, 'https://i.ibb.co/27F2ZVBf/sport-marathon-centre-ville-3.webp',
        'https://ibb.co/TBwXWj5F/a9a22f254805336ef50bedaa56d68a7f', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (58, 9, 'https://i.ibb.co/ksJ7PRVZ/sport-marathon-centre-ville-4.webp',
        'https://ibb.co/9khDdFmP/19566a13de2494c8a7fc913e1aa08963', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (59, 10, 'https://i.ibb.co/5gzRcKTP/sport-tournoi-futsal-nocturne-3.webp',
        'https://ibb.co/W4d2KVnL/5b9bcc5a0b6c9027da229e1e0f4547a0', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (60, 11, 'https://i.ibb.co/HfVJR8m5/sport-yoga-lever-du-soleil-2.webp',
        'https://ibb.co/0Rcw1bPH/66ffb3c10101f7afb9628a77fa64b743', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (61, 11, 'https://i.ibb.co/Dfc84L10/sport-yoga-lever-du-soleil-3.webp',
        'https://ibb.co/chfCJxy4/c66b14e624da0f167aa96d85d7c63699', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (62, 11, 'https://i.ibb.co/mrywhRrT/sport-yoga-lever-du-soleil-4.webp',
        'https://ibb.co/p6K8416w/3a48a56e3da55d4ce289715201de57a3', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (63, 12, 'https://i.ibb.co/bRKZqWJt/loisir-escape-game-mystere-egyptien-2.webp',
        'https://ibb.co/tPJtkmXR/d894e7f81d31e31a0e1e09b6f7de15da', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (64, 12, 'https://i.ibb.co/fVw0SkQS/loisir-escape-game-mystere-egyptien-3.webp',
        'https://ibb.co/Q7wmcPXc/1996fcf21361048ccf76cc119b457257', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (65, 13, 'https://i.ibb.co/cXXVxjDG/loisir-brunch-fleuri-jardin-2.webp',
        'https://ibb.co/Vpp53s27/dd54f20e07f64e7c64c630b8a299186b', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (66, 13, 'https://i.ibb.co/6cZ2ynQC/loisir-brunch-fleuri-jardin-3.webp',
        'https://ibb.co/Myhbg2d0/ae91b90f13c10c544a67593f770246f9', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (67, 13, 'https://i.ibb.co/fYy3VrNf/loisir-brunch-fleuri-jardin-4.webp',
        'https://ibb.co/5WSQXsjm/66f1e326e863a8c9123cfb6cb8b91d88', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (68, 14, 'https://i.ibb.co/6JvtdWfn/loisir-atelier-poterie-2.webp',
        'https://ibb.co/JFxphsMB/26c3f779cfd8ad04741f6e15f87db735', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (69, 14, 'https://i.ibb.co/RpFwZCpH/loisir-atelier-poterie-3.webp',
        'https://ibb.co/8DCZTjD8/1fe63b92d445358c62a88c9705ff03e8', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (70, 15, 'https://i.ibb.co/F4BX93V4/sport-initiation-escalade-bloc-2.webp',
        'https://ibb.co/v4JhbqY4/45119280e2917b4fb309ad6b736cad27', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (71, 15, 'https://i.ibb.co/YF8zgM4k/sport-initiation-escalade-bloc-3.webp',
        'https://ibb.co/fzthJ5Vp/fd6065d9ea9da7e3946ea01cd0eb4c10', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (72, 15, 'https://i.ibb.co/840LfTXz/sport-initiation-escalade-bloc-4.webp',
        'https://ibb.co/3Y79jGkf/5529bdf123a6cdcfe925255b0c97944e', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (73, 16, 'https://i.ibb.co/MykV4m9f/sport-paddle-coucher-du-soleil-2.webp',
        'https://ibb.co/DPfCyZLY/c032a1c687b0e8b0971ca6eda2de26d9', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (74, 16, 'https://i.ibb.co/Zqj5rbK/sport-paddle-coucher-du-soleil-3.webp',
        'https://ibb.co/h36bNTH/fd605f0559892925012e8fe37712a01f', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (75, 17, 'https://i.ibb.co/4nGTssDp/sport-initiation-tir-a-l-arc-2.webp',
        'https://ibb.co/v6GPssrk/4e5932b1f1f6a705194f743e2fb2685a', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (76, 17, 'https://i.ibb.co/6JHzsLrv/sport-initiation-tir-a-l-arc-3.webp',
        'https://ibb.co/sJKn3zb5/9b107aac6300aa740da69af3cab02580', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (77, 17, 'https://i.ibb.co/1Gm47bF0/sport-initiation-tir-a-l-arc-4.webp',
        'https://ibb.co/TMgNYr9w/3ee4913974a6c466d0b251e266d91da6', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (78, 18, 'https://i.ibb.co/tphdtNp5/culture-soiree-lecture-librairie-2.webp',
        'https://ibb.co/SwxL81wp/4c1939f8b9a0e7c54ddc6958d8547fa3', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (79, 18, 'https://i.ibb.co/zhft7zNB/culture-soiree-lecture-librairie-3.webp',
        'https://ibb.co/C50SMfWY/01f6b9f242dab6e8f54ea98c63c7fbf4', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (80, 19, 'https://i.ibb.co/h1drgCgb/culture-festival-street-art-2.webp',
        'https://ibb.co/rfpqQvQY/e12ef83b201a893852263e928395b8a2', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (81, 19, 'https://i.ibb.co/60rCf7W1/culture-festival-street-art-3.webp',
        'https://ibb.co/gZmKBLRP/4f04ba31e6b5d85b52978430d060fc1d', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (82, 19, 'https://i.ibb.co/QvHv86tR/culture-festival-street-art-4.webp',
        'https://ibb.co/RT6ThCdm/8e72c2316b845a0a3e4d201e6b4b80d8', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (83, 20, 'https://i.ibb.co/0Rx0mmm6/culture-visite-theatralisee-chateau-2.webp',
        'https://ibb.co/wFfnBBBP/944bd55a95a056852322b9b02ce78148', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (84, 20, 'https://i.ibb.co/GfT740Bz/culture-visite-theatralisee-chateau-3.webp',
        'https://ibb.co/HLd7fV15/5ce23b3c07e125f82cda16834bf0b9e2', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (85, 21, 'https://i.ibb.co/prPYR91q/loisir-atelier-cuisine-du-monde-2.webp',
        'https://ibb.co/Xk4c3vWH/3da8d894f59553feec96e06fa0bf88f7', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (86, 21, 'https://i.ibb.co/bMvXKvYd/loisir-atelier-cuisine-du-monde-3.webp',
        'https://ibb.co/xtg67gw5/03940a558c89747cecab3969b6afd951', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (87, 21, 'https://i.ibb.co/jv0TQDZY/loisir-atelier-cuisine-du-monde-4.webp',
        'https://ibb.co/Y7n31hTS/8e221a30ff0cf607a4a6d86e7c8894a7', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (88, 22, 'https://i.ibb.co/mrtxhYYh/loisir-soiree-karaoke-retro-2.webp',
        'https://ibb.co/7xV9rTTr/bf6fbd021fe2aca8d7592c57f28a9530', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (89, 22, 'https://i.ibb.co/mVNzZMCG/loisir-soiree-karaoke-retro-3.webp',
        'https://ibb.co/G4HPYbv0/37df4a59ebfba1114692e6cd2930dce4', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (90, 23, 'https://i.ibb.co/hJ86tRDm/loisir-atelier-creation-terrarium-2.webp',
        'https://ibb.co/bMd08gzK/7245425b323249102ee2a103558c518e', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (91, 23, 'https://i.ibb.co/m5PVvVcy/loisir-atelier-creation-terrarium-3.webp',
        'https://ibb.co/23p181kM/7b8f308b5bc59f24e588874895f47966', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (92, 23, 'https://i.ibb.co/rKG15P3b/loisir-atelier-creation-terrarium-4.webp',
        'https://ibb.co/vCxKL5hs/4dea52ca32614b7992c2074aa72b3b7d', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (93, 24, 'https://i.ibb.co/99ZRSVh8/loisir-nuit-observation-etoiles-2.webp',
        'https://ibb.co/8DYFtdbK/6b3f33af0d2d8918e456b826f5f18f6e', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (94, 24, 'https://i.ibb.co/n8ZTnRqD/loisir-nuit-observation-etoiles-3.webp',
        'https://ibb.co/84TJdXnj/1c682474913455e65db5bfe56e7f62e7', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (95, 25, 'https://i.ibb.co/VW6p7rQr/sport-initiation-aviron-lever-du-soleil-2.webp',
        'https://ibb.co/8Dk4WTdT/13d98e5b1f7123239f63b1d181ba00fc', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (96, 25, 'https://i.ibb.co/PsG8b9MQ/sport-initiation-aviron-lever-du-soleil-3.webp',
        'https://ibb.co/5XhVznkF/8c45b69c811afeb0ea5a84be09cc0a47', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (97, 25, 'https://i.ibb.co/jZyL6ZJ8/sport-initiation-aviron-lever-du-soleil-4.webp',
        'https://ibb.co/Pzc5mzDr/8d17c2734a6de69cb4fdfd56cfe5c7f3', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (98, 26, 'https://i.ibb.co/HpR9QWKz/sport-tournoi-beach-volley-2.webp',
        'https://ibb.co/x8wrQp2j/714cdd252d1f0a1041c911c63af7f220', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (99, 26, 'https://i.ibb.co/TxSZ8WTQ/sport-tournoi-beach-volley-3.webp',
        'https://ibb.co/Xrg9sY4n/f615907b639c114aead8b3742b50fa68', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (100, 27, 'https://i.ibb.co/pv9MkRwP/sport-initiation-parkour-urbain-2.webp',
        'https://ibb.co/TBF7Nb2T/45077b91d76616fe19ff0fd70d209587', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (101, 27, 'https://i.ibb.co/TdjWnqb/sport-initiation-parkour-urbain-3.webp',
        'https://ibb.co/MQwBj5G/30f0ddfee57d451dc859c0f477c2bfa9', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (102, 27, 'https://i.ibb.co/6RDkbQh3/sport-initiation-parkour-urbain-4.webp',
        'https://ibb.co/WWBLFC9b/ed8190ef7aa0259970cdadb5abbd1d32', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (103, 28, 'https://i.ibb.co/Y7HDtt78/culture-atelier-calligraphie-japonaise-2.webp',
        'https://ibb.co/SXFQKKX0/ec5efc3b0dbdfdd7c9ec9973436093d9', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (104, 28, 'https://i.ibb.co/TxrhjyPV/culture-atelier-calligraphie-japonaise-3.webp',
        'https://ibb.co/8LX8vkPW/2ba0bd4390c446d240b044a08bcbd644', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (105, 29, 'https://i.ibb.co/PZRnDfZs/culture-balade-photographique-vieux-village-2.webp',
        'https://ibb.co/MDb4B3Dy/c520acf1b011ff1eee8f2cfb2c758b6d', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (106, 29, 'https://i.ibb.co/1fCH6sT3/culture-balade-photographique-vieux-village-3.webp',
        'https://ibb.co/VWFXmHTd/029a7610ac3663b479969da9598dca20', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (107, 29, 'https://i.ibb.co/Kp9QWS7c/culture-balade-photographique-vieux-village-4.webp',
        'https://ibb.co/fzCKHLtV/741d4d75b505cfb7a648359827d5df5e', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (108, 30, 'https://i.ibb.co/3Y4mHRPW/culture-fete-lanternes-bord-de-l-eau-2.webp',
        'https://ibb.co/84NgVYQ2/37e94ad00e5d40939565214ea4444855', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (109, 30, 'https://i.ibb.co/QFMn3mDw/culture-fete-lanternes-bord-de-l-eau-3.webp',
        'https://ibb.co/KjW7pyKv/19c9fb571a1df14f7d4f966db1ac04ef', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (110, 31, 'https://i.ibb.co/TDqBSpjK/loisir-atelier-creation-parfum-2.webp',
        'https://ibb.co/PszZn3XT/ef78db3652180e91db3bc787cd202b53', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (111, 31, 'https://i.ibb.co/BVB01Pkn/loisir-atelier-creation-parfum-3.webp',
        'https://ibb.co/FbzRPWpH/2d61768c643f3fedba17a2b8ccf57c7d', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (112, 31, 'https://i.ibb.co/bgHry9pd/loisir-atelier-creation-parfum-4.webp',
        'https://ibb.co/8DzjRZF8/56a01cf96222586e0c3257fe54f27e73', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (113, 32, 'https://i.ibb.co/WWKgHk77/loisir-decouverte-apiculture-2.webp',
        'https://ibb.co/qM1kDNhh/6480042b8fd210292c2846fafb607859', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (114, 32, 'https://i.ibb.co/271dDQbB/loisir-decouverte-apiculture-3.webp',
        'https://ibb.co/SX4Bh8Lp/1b7403c9855e5b992166c14b61d3f95d', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (115, 33, 'https://i.ibb.co/MD1YJ70Q/loisir-atelier-bougies-parfumees-2.webp',
        'https://ibb.co/tM271DrF/93b0d66cb9e2c1f27eadeeb2bfb248f2', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (116, 33, 'https://i.ibb.co/KjYr6D2C/loisir-atelier-bougies-parfumees-3.webp',
        'https://ibb.co/tMSJBzqy/20ec6752799a9ec51a3c16d8db2c1ab1', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (117, 33, 'https://i.ibb.co/spR87Pny/loisir-atelier-bougies-parfumees-4.webp',
        'https://ibb.co/BH30kPbq/b416e3ebb6174c75ff6e9b5b2554beb2', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (118, 34, 'https://i.ibb.co/zH62hJfg/loisir-soiree-quiz-pop-culture-2.webp',
        'https://ibb.co/dJmp0gKv/eb5e3c72d5ec5ca23d0a604433e60097', FALSE);

INSERT INTO images (id, event_id, url, delete_url, is_preview)
VALUES (119, 34, 'https://i.ibb.co/MFPDYFW/loisir-soiree-quiz-pop-culture-3.webp',
        'https://ibb.co/h32RP3h/fc7c6f1dbed66832c17e698ded69cf4c', FALSE);

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
