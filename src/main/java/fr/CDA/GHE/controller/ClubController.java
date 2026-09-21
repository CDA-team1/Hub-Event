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
 * Seuls les rôles sont exprimés ici via {@code @Secured} (actif grâce à
 * {@code @EnableMethodSecurity(securedEnabled = true)} dans {@code SecurityConfig}) ; les
 * routes publiques (GET) n'ont pas d'annotation, elles sont en {@code permitAll} côté
 * {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/clubs")
public class ClubController implements ClubControllerDoc {

    private final ClubService clubService;

    public ClubController(ClubService clubService){
        this.clubService = clubService;
    }

    // Public
    @Override
    @GetMapping
    public PageDto<ClubDto> getAll(Pageable pageable) {
        return clubService.extractAll(pageable);
    }

    // Public
    @Override
    @GetMapping("/{id}")
    public ClubDto getById(@PathVariable Long id) {
        return clubService.extractById(id);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClubDto create(@RequestBody ClubDto clubDto) throws FunctionalException {
       return clubService.create(clubDto);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public ClubDto update(@PathVariable Long id, @RequestBody ClubDto clubDto) throws FunctionalException {
        return clubService.update(id, clubDto);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws FunctionalException {
        clubService.delete(id);
    }
}
