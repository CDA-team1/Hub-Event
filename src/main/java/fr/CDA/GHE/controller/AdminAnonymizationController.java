package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.AnonymizationRequestService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes d'administration des demandes d'anonymisation.
 * <p>
 * Toutes les routes sont réservées à l'admin (consultation CU28, validation CU29 à venir).
 * La création d'une demande par un membre est une route distincte et non réservée :
 * {@code POST /anonymization-requests}, voir {@link AnonymizationRequestController}. Même
 * séparation self-service / admin que pour les comptes utilisateurs (CPT-05, {@link UserController}).
 */
@RestController
@RequestMapping("/admin/anonymization")
public class AdminAnonymizationController implements AdminAnonymizationControllerDoc {

    private final AnonymizationRequestService anonymizationRequestService;

    public AdminAnonymizationController(AnonymizationRequestService anonymizationRequestService) {
        this.anonymizationRequestService = anonymizationRequestService;
    }

    @Override
    @Secured("ROLE_ADMIN")
    @GetMapping
    public PageDto<AnonymizationDto> getAll(Pageable pageable) {
        return anonymizationRequestService.extractAll(pageable);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PostMapping("/{id}/validate")
    public AnonymizationDto validate(@PathVariable Long id) throws FunctionalException {
        return anonymizationRequestService.validateRequest(id);
    }
}
