package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.AnonymizationRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * SEC-02 — Autorisations des routes demandes d'anonymisation (voir {@link ClubController} pour les règles générales).
 * <p>
 * {@code create} : un utilisateur connecté demande l'anonymisation de SON compte (identifié via le
 * JWT, jamais transmis dans le corps de la requête) → pas de restriction de rôle.
 * La consultation et la validation sont réservées à l'ADMIN, sous un préfixe distinct :
 * {@code /admin/anonymization}, voir {@link AdminAnonymizationController}. Même séparation
 * self-service / admin que pour les comptes utilisateurs (CPT-05, {@link UserController}).
 */
@RestController
@RequestMapping("/anonymization-requests")
public class AnonymizationRequestController implements AnonymizationRequestControllerDoc {

    private final AnonymizationRequestService anonymizationRequestService;

    public AnonymizationRequestController(AnonymizationRequestService anonymizationRequestService) {
        this.anonymizationRequestService = anonymizationRequestService;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnonymizationDto create() throws FunctionalException {
        return anonymizationRequestService.createRequest();
    }
}
