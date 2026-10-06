package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.ClubService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SEC-02 — Autorisations des routes des clubs.
 * <p>
 * Toute la gestion des clubs (CU24, CdC p.10 "Liste des clubs") est réservée à l'administrateur
 * : {@code @Secured("ROLE_ADMIN")} s'applique à toutes les routes par défaut (actif grâce à
 * {@code @EnableMethodSecurity(securedEnabled = true)} dans {@code SecurityConfig}, qui ne place
 * plus {@code /clubs} en {@code permitAll}), à l'exception de {@code GET /clubs/mine} (EVT-06) :
 * son {@code @Secured} de méthode prend le pas sur celui de la classe pour cette seule route,
 * réservée à l'organisateur (choix du club à la création d'un événement).
 * </p>
 */
@RestController
@RequestMapping("/clubs")
@Secured("ROLE_ADMIN")
public class ClubController implements ClubControllerDoc {

    private final ClubService clubService;

    public ClubController(ClubService clubService){
        this.clubService = clubService;
    }

    @Override
    @GetMapping
    public PageDto<ClubDto> getAll(Pageable pageable) {
        return clubService.extractAll(pageable);
    }

    @Override
    @Secured("ROLE_ORGANIZER")
    @GetMapping("/mine")
    public List<ClubDto> getMine() {
        return clubService.extractMine();
    }

    @Override
    @GetMapping("/{id}")
    public ClubDto getById(@PathVariable Long id) {
        return clubService.extractById(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClubDto create(@RequestBody ClubDto clubDto) throws FunctionalException {
       return clubService.create(clubDto);
    }

    @Override
    @PutMapping("/{id}")
    public ClubDto update(@PathVariable Long id, @RequestBody ClubDto clubDto) throws FunctionalException {
        return clubService.update(id, clubDto);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws FunctionalException {
        clubService.delete(id);
    }
}
