package fr.CDA.GHE.service;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.entity.Club;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.exception.NotFoundException;
import fr.CDA.GHE.mapper.ClubMapper;
import fr.CDA.GHE.repository.ClubRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère la création, la modification, la suppression et la consultation des clubs.
 * <p>
 * Réservé à l'administrateur (contrôle du rôle fait au niveau du controller via
 * {@code @Secured}, voir SEC-02).
 * </p>
 */
@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;

    public ClubService(ClubRepository clubRepository, ClubMapper clubMapper){
        this.clubRepository = clubRepository;
        this.clubMapper = clubMapper;
    }

    @Transactional(readOnly = true)
    public PageDto<ClubDto> extractAll(Pageable pageable){
        Page<Club> page = clubRepository.findAll(pageable);
        return new PageDto<>(
                clubMapper.toDtoList(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ClubDto extractById(Long id){
        Club club = clubRepository.findById(id).orElseThrow(() -> new NotFoundException("Club introuvable"));
        return clubMapper.toDto(club);
    }

    @Transactional
    public ClubDto create(ClubDto dto) throws FunctionalException{
        validate(dto);

        Club club = new Club(dto.name(), dto.category(), dto.postalAddress(), dto.email(), dto.phone());
        Club created = clubRepository.save(club);
        return clubMapper.toDto(created);
    }

    @Transactional
    public ClubDto update(Long id, ClubDto dto) throws FunctionalException{
        validate(dto);

        Club club = clubRepository.findById(id).orElseThrow(() -> new NotFoundException("Club introuvable"));

        club.setName(dto.name());
        club.setCategory(dto.category());
        club.setPostalAddress(dto.postalAddress());
        club.setEmail(dto.email());
        club.setPhone(dto.phone());
        // validityEndDate volontairement ignorée : non saisissable, calculée par le système.

        return clubMapper.toDto(club);
    }

    @Transactional
    public void delete(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Club introuvable"));
        clubRepository.delete(club);
    }

    private void validate(ClubDto dto) throws FunctionalException {
        if (isBlank(dto.name())) {
            throw new FunctionalException("Le nom du club est obligatoire");
        }
        if (dto.category() == null) {
            throw new FunctionalException("La catégorie du club est obligatoire");
        }
        if (isBlank(dto.postalAddress())) {
            throw new FunctionalException("L'adresse postale du club est obligatoire");
        }
        if (isBlank(dto.email())) {
            throw new FunctionalException("L'adresse email du club est obligatoire");
        }
        if (isBlank(dto.phone())) {
            throw new FunctionalException("Le numéro de téléphone du club est obligatoire");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
