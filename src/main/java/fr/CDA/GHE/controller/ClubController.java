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
 * SEC-02 — Câblage des autorisations sur les routes des clubs (matrice {@code docs/SEC-02-matrice-routes.md}).
 * <p>
 * Seuls les rôles sont exprimés ici via {@code @Secured} ; les routes publiques (GET) n'ont pas
 * d'annotation (elles seront en {@code permitAll} dans le {@code SecurityConfig} de SEC-01).
 * <p>
 * ⚠️ Les {@code @Secured} restent inertes tant que SEC-01 n'a pas activé
 * {@code @EnableMethodSecurity(securedEnabled = true)}. Les corps sont des TODO : la logique
 * métier sera remplie par la tâche feature, <strong>sans retirer les annotations de sécurité</strong>.
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
