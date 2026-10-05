package fr.CDA.GHE.controller;

import fr.CDA.GHE.dto.ClubDto;
import fr.CDA.GHE.dto.PageDto;
import fr.CDA.GHE.exception.FunctionalException;
import fr.CDA.GHE.service.ClubService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * SEC-02 — Autorisations des routes des clubs.
 * <p>
 * Toute la gestion des clubs (CU24, CdC p.10 "Liste des clubs") est réservée à l'administrateur
 * : aucune route de ce contrôleur n'est publique, {@code @Secured("ROLE_ADMIN")} s'applique à
 * toutes (actif grâce à {@code @EnableMethodSecurity(securedEnabled = true)} dans
 * {@code SecurityConfig}, qui ne place plus {@code /clubs} en {@code permitAll}).
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
