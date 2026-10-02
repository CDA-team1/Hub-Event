-- OPS-02 - Script de création de la base de données
-- Hub événementiel
-- MySQL 8.x
--
-- Ce fichier documente la structure SQL correspondant aux entités JPA actuelles.
-- Il n'est pas exécuté automatiquement par Spring Boot.

CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       lastname VARCHAR(255) NOT NULL,
                       firstname VARCHAR(255) NOT NULL,
                       postal_address VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       phone VARCHAR(255),
                       password VARCHAR(255) NOT NULL,
                       status ENUM('ACTIVE', 'ANONYMIZED', 'INACTIVE') NOT NULL,
                       role ENUM('ADMIN', 'MEMBER', 'ORGANIZER') NOT NULL,
                       suspended BIT NOT NULL,
                       suspension_end_date DATE,
                       suspension_reason VARCHAR(255),
                       activation_token VARCHAR(255),
                       pending_password VARCHAR(255),
                       password_change_token VARCHAR(255),

                       PRIMARY KEY (id),
                       UNIQUE (email),
                       UNIQUE (activation_token),
                       UNIQUE (password_change_token)
) ENGINE=InnoDB;


CREATE TABLE clubs (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(255) NOT NULL,
                       category ENUM('CULTURE', 'LEISURE', 'SPORT') NOT NULL,
                       postal_address VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       phone VARCHAR(255) NOT NULL,
                       validity_end_date DATE,

                       PRIMARY KEY (id)
) ENGINE=InnoDB;


CREATE TABLE affiliation (
                             club_id BIGINT NOT NULL,
                             user_id BIGINT NOT NULL,

                             PRIMARY KEY (club_id, user_id),

                             CONSTRAINT fk_affiliation_club
                                 FOREIGN KEY (club_id)
                                     REFERENCES clubs (id),

                             CONSTRAINT fk_affiliation_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users (id)
) ENGINE=InnoDB;


CREATE TABLE events (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        title VARCHAR(255) NOT NULL,
                        description TEXT NOT NULL,
                        location VARCHAR(255) NOT NULL,
                        start_date_time DATETIME(6) NOT NULL,
                        end_date_time DATETIME(6),
                        affiliated_price DECIMAL(38, 2) NOT NULL,
                        non_affiliated_price DECIMAL(38, 2) NOT NULL,
                        max_seats INTEGER NOT NULL,
                        status ENUM('CANCELLED', 'DRAFT', 'FINISHED', 'PUBLISHED') NOT NULL,
                        category ENUM('CULTURE', 'LEISURE', 'SPORT') NOT NULL,
                        organizer_id BIGINT NOT NULL,
                        club_id BIGINT NOT NULL,

                        PRIMARY KEY (id),

                        CONSTRAINT fk_event_organizer
                            FOREIGN KEY (organizer_id)
                                REFERENCES users (id),

                        CONSTRAINT fk_event_club
                            FOREIGN KEY (club_id)
                                REFERENCES clubs (id)
) ENGINE=InnoDB;


CREATE TABLE registrations (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               event_id BIGINT NOT NULL,
                               status ENUM('REGISTERED', 'WAITING_LIST') NOT NULL,
                               registration_date DATETIME(6) NOT NULL,

                               PRIMARY KEY (id),
                               UNIQUE (user_id, event_id),

                               CONSTRAINT fk_registration_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users (id),

                               CONSTRAINT fk_registration_event
                                   FOREIGN KEY (event_id)
                                       REFERENCES events (id)
) ENGINE=InnoDB;


CREATE TABLE comments (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          content TEXT NOT NULL,
                          user_id BIGINT NOT NULL,
                          event_id BIGINT NOT NULL,
                          created_at DATETIME(6) NOT NULL,

                          PRIMARY KEY (id),

                          CONSTRAINT fk_comment_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users (id),

                          CONSTRAINT fk_comment_event
                              FOREIGN KEY (event_id)
                                  REFERENCES events (id)
) ENGINE=InnoDB;


CREATE TABLE images (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        event_id BIGINT NOT NULL,
                        url VARCHAR(255) NOT NULL,
                        delete_url VARCHAR(255) NOT NULL,
                        is_preview BIT NOT NULL DEFAULT FALSE,

                        PRIMARY KEY (id),

                        CONSTRAINT fk_image_event
                            FOREIGN KEY (event_id)
                                REFERENCES events (id)
) ENGINE=InnoDB;


CREATE TABLE legal_documents (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 type ENUM('CGU', 'RGPD') NOT NULL,
                                 content TEXT NOT NULL,
                                 updated_at DATETIME(6) NOT NULL,

                                 PRIMARY KEY (id),
                                 UNIQUE (type)
) ENGINE=InnoDB;


CREATE TABLE anonymization_requests (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        user_id BIGINT NOT NULL,
                                        status ENUM('PENDING', 'VALIDATED') NOT NULL,
                                        request_date DATETIME(6) NOT NULL,

                                        PRIMARY KEY (id),
                                        UNIQUE (user_id),

                                        CONSTRAINT fk_anonymization_request_user
                                            FOREIGN KEY (user_id)
                                                REFERENCES users (id)
) ENGINE=InnoDB;