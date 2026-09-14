package fr.CDA.GHE.repository;

import fr.CDA.GHE.entity.Event;
import fr.CDA.GHE.entity.Registration;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository JPA pour l'entité {@link Registration}.
 */
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

  List<Registration> findByEvent(Event event);

  List<Registration> findByUser(User user);

  long countByEventAndStatus(Event event, RegistrationStatus status);
}