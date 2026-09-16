package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.AnonymizationDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.entity.AnonymizationRequest;
import fr.CDA.GHE.entity.User;
import fr.CDA.GHE.entity.enums.AccountStatus;
import fr.CDA.GHE.entity.enums.RequestStatus;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.AnonymizationRequestMapper;
import fr.CDA.GHE.repository.AnonymizationRequestRepository;
import fr.CDA.GHE.repository.UserRepository;
import fr.CDA.GHE.util.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gère les demandes d'anonymisation des utilisateurs (CU14, CU28, CU29).
 */
@Service
public class AnonymizationRequestService {

    private static final Logger log = LoggerFactory.getLogger(AnonymizationRequestService.class);

    private final AnonymizationRequestRepository anonymizationRequestRepository;
    private final UserRepository userRepository;
    private final AnonymizationRequestMapper anonymizationRequestMapper;

    public AnonymizationRequestService(AnonymizationRequestRepository anonymizationRequestRepository,
                                        UserRepository userRepository,
                                        AnonymizationRequestMapper anonymizationRequestMapper) {
        this.anonymizationRequestRepository = anonymizationRequestRepository;
        this.userRepository = userRepository;
        this.anonymizationRequestMapper = anonymizationRequestMapper;
    }

    /**
     * Enregistre une demande d'anonymisation pour l'utilisateur connecté (CU14, SFG §2.17).
     * <p>
     * La demande ne provoque pas immédiatement l'anonymisation du compte : elle reste
     * {@link RequestStatus#PENDING} jusqu'à ce qu'un administrateur la valide (CU29).
     *
     * @return la demande créée
     * @throws FunctionalException si le compte n'est pas actif, ou si une demande existe déjà
     */
    @Transactional
    public AnonymizationDto createRequest() throws FunctionalException {
        User user = userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new FunctionalException("Votre compte doit être actif pour demander l'anonymisation de vos données.");
        }

        if (anonymizationRequestRepository.findByUser(user).isPresent()) {
            throw new FunctionalException("Une demande d'anonymisation est déjà en cours pour votre compte.");
        }

        AnonymizationRequest request = new AnonymizationRequest(user, RequestStatus.PENDING, LocalDateTime.now());
        AnonymizationRequest created = anonymizationRequestRepository.save(request);

        log.info("CREATION demande d'anonymisation : id={} userEmail={}", created.getId(), user.getEmail());

        return anonymizationRequestMapper.toDto(created);
    }

    /**
     * Retourne une page de demandes d'anonymisation, avec les informations de l'utilisateur
     * concerné pour chacune (CU28, SFG §2.31).
     *
     * @param pageable pagination demandée
     * @return la page de demandes correspondante
     */
    @Transactional(readOnly = true)
    public PageDto<AnonymizationDto> extractAll(Pageable pageable) {
        Page<AnonymizationRequest> page = anonymizationRequestRepository.findAll(pageable);
        List<AnonymizationDto> content = anonymizationRequestMapper.toDtoList(page.getContent());
        return new PageDto<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
