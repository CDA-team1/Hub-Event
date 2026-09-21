package fr.CDA.GHE.service;

import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.Category;
import fr.CDA.GHE.entity.enums.EventStatus;
import fr.CDA.GHE.entity.enums.Role;
import fr.CDA.GHE.repository.ClubRepository;
import fr.CDA.GHE.repository.EventRepository;
import fr.CDA.GHE.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static fr.CDA.GHE.entity.enums.Category.SPORT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests des prédicats de recherche multicritère (EventSpecifications), vérifiés
 * via de vraies requêtes exécutées sur la base H2 de test.
 * <p>
 * {@code @Transactional} annule les écritures après chaque test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventSpecificationsTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    private User organizer;
    private Club club;

    @BeforeEach
    void setUp() {
        User newOrganizer = new User(
                "Dupont", "Jean", "1 rue de Paris", "organizer@test.fr",
                "0600000000", "password", Role.ORGANIZER
        );
        newOrganizer.setStatus(AccountStatus.ACTIVE);
        organizer = userRepository.save(newOrganizer);

        club = clubRepository.save(new Club(
                "Club Test", SPORT, "1 rue du Club", "club@test.fr", "0600000001"
        ));
    }

    private Event persistEvent(
            String title,
            Category category,
            EventStatus status,
            LocalDateTime startDateTime,
            BigDecimal nonAffiliatedPrice,
            String location
    ) {
        Event event = new Event(
                title, "Description de test", location, startDateTime, null,
                BigDecimal.TEN, nonAffiliatedPrice, 50, category, organizer, club
        );

        switch (status) {
            case PUBLISHED -> event.publish();
            case FINISHED -> event.finish();
            case CANCELLED -> event.cancel();
            case DRAFT -> { }
        }

        return eventRepository.save(event);
    }

    /**
     * Vérifie que EventSpecifications.hasStatus(PUBLISHED) ne retourne
     * que les événements publiés, en excluant les brouillons.
     */
    @Test
    void shouldReturnOnlyPublishedEvents(){

        // Arrange : un événement publié, un événement brouillon
        persistEvent(
                "Concert publié", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 20, 20, 0), BigDecimal.valueOf(15), "Lyon"
        );

        persistEvent(
                "Tournoi brouillon", SPORT, EventStatus.DRAFT,
                LocalDateTime.of(2026, 12, 5, 14, 0), BigDecimal.valueOf(10), "Nîmes"
        );

        // Act
        List<Event> result = eventRepository.findAll(
                EventSpecifications.hasStatus(EventStatus.PUBLISHED)
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Concert publié", result.get(0).getTitle());
    }

    /**
     * Vérifie que EventSpecifications.hasCategory(SPORT) ne retourne
     * que les événements de cette catégorie, en excluant les autres.
     */
    @Test
    void shouldFilterByCategory(){

        // Arrange : un événement de catégorie CULTURE, un de catégorie SPORT
        persistEvent(
                "Concert publié", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 20, 20, 0), BigDecimal.valueOf(15), "Lyon"
        );

        persistEvent(
                "Tournoi brouillon", SPORT, EventStatus.DRAFT,
                LocalDateTime.of(2026, 12, 5, 14, 0), BigDecimal.valueOf(10), "Nîmes"
        );

        // Act
        List<Event> result = eventRepository.findAll(
                EventSpecifications.hasCategory(SPORT)
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Tournoi brouillon", result.get(0).getTitle());
    }

    /**
     * Vérifie que EventSpecifications.hasMinPrice(...) exclut les événements
     * dont le prix est inférieur au seuil demandé.
     */
    @Test
    void shouldFilterByMinPrice(){

        // Arrange : un événement pas cher, un événement cher
        persistEvent(
                "Événement pas cher", Category.LEISURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 20, 20, 0), BigDecimal.valueOf(5), "Lyon"
        );
        persistEvent(
                "Événement cher", Category.LEISURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 14, 0), BigDecimal.valueOf(20), "Nîmes"
        );

        // Act
        List<Event> result = eventRepository.findAll(
                EventSpecifications.hasMinPrice(BigDecimal.valueOf(10))
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Événement cher", result.get(0).getTitle());
    }

    /**
     * Vérifie que EventSpecifications.hasMaxPrice(...) exclut les événements
     * dont le prix est supérieur au seuil demandé.
     */
    @Test
    void shouldFilterByMaxPrice(){

        // Arrange : un événement pas cher, un événement cher
        persistEvent(
                "Événement pas cher", Category.LEISURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 20, 20, 0), BigDecimal.valueOf(5), "Lyon"
        );
        persistEvent(
                "Événement cher", Category.LEISURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 14, 0), BigDecimal.valueOf(20), "Nîmes"
        );

        // Act
        List<Event> result = eventRepository.findAll(
                EventSpecifications.hasMaxPrice(BigDecimal.valueOf(10))
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Événement pas cher", result.get(0).getTitle());
    }

    /**
     * Vérifie que EventSpecifications.hasLocation(...) retourne les événements
     * dont le lieu contient le texte recherché, indépendamment de la casse.
     */
    @Test
    void shouldFilterByLocation(){

        // Arrange : un événement à Lyon, un événement à Marseille
        persistEvent(
                "Concert à Lyon", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 20, 20, 0), BigDecimal.valueOf(15), "Lyon"
        );
        persistEvent(
                "Festival à Marseille", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 14, 0), BigDecimal.valueOf(15), "Marseille"
        );

        // Act : recherche partielle, en minuscules
        List<Event> result = eventRepository.findAll(
                EventSpecifications.hasLocation("lyo")
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Concert à Lyon", result.get(0).getTitle());
    }

    /**
     * Vérifie que EventSpecifications.startsOnOrAfter(...) exclut les événements
     * dont la date de début est antérieure à la borne demandée.
     */
    @Test
    void shouldFilterByStartDate(){

        // Arrange : un événement passé, un événement futur
        persistEvent(
                "Événement passé", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 1, 10, 10, 0), BigDecimal.valueOf(15), "Lyon"
        );
        persistEvent(
                "Événement futur", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 6, 15, 10, 0), BigDecimal.valueOf(15), "Lyon"
        );

        // Act
        List<Event> result = eventRepository.findAll(
                EventSpecifications.startsOnOrAfter(LocalDateTime.of(2026, 3, 1, 0, 0))
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Événement futur", result.get(0).getTitle());
    }

    /**
     * Vérifie que EventSpecifications.startsOnOrBefore(...) exclut les événements
     * dont la date de début est postérieure à la borne demandée.
     */
    @Test
    void shouldFilterByEndDate(){

        // Arrange : un événement passé, un événement futur
        persistEvent(
                "Événement passé", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 1, 10, 10, 0), BigDecimal.valueOf(15), "Lyon"
        );
        persistEvent(
                "Événement futur", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 6, 15, 10, 0), BigDecimal.valueOf(15), "Lyon"
        );

        // Act
        List<Event> result = eventRepository.findAll(
                EventSpecifications.startsOnOrBefore(LocalDateTime.of(2026, 3, 1, 23, 59, 59))
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Événement passé", result.get(0).getTitle());
    }

    /**
     * Vérifie que EventSpecifications.hasKeywords(...) retourne les événements
     * dont le titre (ou la description) contient le mot-clé recherché.
     */
    @Test
    void shouldFilterByKeywords(){

        // Arrange : un événement contenant le mot-clé dans son titre, un autre non
        persistEvent(
                "Tournoi de tennis", Category.SPORT, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 20, 20, 0), BigDecimal.valueOf(15), "Lyon"
        );
        persistEvent(
                "Concert de musique", Category.CULTURE, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 5, 14, 0), BigDecimal.valueOf(15), "Marseille"
        );

        // Act
        List<Event> result = eventRepository.findAll(
                EventSpecifications.hasKeywords("tennis")
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Tournoi de tennis", result.get(0).getTitle());
    }
}