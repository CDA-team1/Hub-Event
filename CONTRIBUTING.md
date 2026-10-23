# Contribuer : règles Trello et Git

Fiche d'équipe qui décrit **comment on travaille réellement** sur le projet. Elle remplace la fiche de départ « Règles Trello & Git - Hub Évènementiel » (PDF), dont plusieurs points ne correspondaient plus à la pratique (voir [§ 8](#8-ce-qui-a-changé-depuis-la-fiche-de-départ)).

## 1. Le tableau Trello

Tableau **Scrumboard — Hub Événementiel**. Une carte = une tâche. Elle avance de gauche à droite :

| Colonne | Sens |
|---|---|
| **A faire** | tâches identifiées, pas encore planifiées |
| **Sprint** | tâches retenues pour le sprint en cours |
| **En cours** | quelqu'un travaille dessus (il se l'assigne) |
| **En revue** | la Pull Request est ouverte |
| **Terminé** | fusionnée dans `dev` |
| **Bloqué** | en attente d'une décision ou d'une autre tâche |
| **Optionnel** | utile mais non prioritaire, à faire si le temps le permet |

## 2. Étiquettes

| Famille | Valeurs |
|---|---|
| Piste | `Back` · `Front` · `Config/Devops` · `Security` · `Test` · `Conception` |
| Priorité | `High priority` · `Medium priority` · `Low priority` |

## 3. Identifiant de carte

Format `ÉPOPÉE-NN`, par exemple `EVT-06`. Cet identifiant relie la carte, la branche et la Pull Request.

Préfixes en usage : `SOCLE` · `SEC` · `AUTH` · `CPT` (comptes) · `EVT` (évènements) · `REG` (inscriptions) · `SEARCH` · `CAL` · `COM` · `CLUB` · `RGPD` · `ANON` · `SUSP` · `OPS` · `UI` · `DOC` · `FRONT`.

## 4. Branches

| Branche | Rôle |
|---|---|
| `main` | versions livrées. Mise à jour ponctuellement par une Pull Request `dev` → `main`. On n'y commite jamais directement. |
| `dev` | branche d'**intégration** : toutes les Pull Requests de travail visent `dev`. |
| `type/ID-slug` | une branche par carte, créée depuis un `dev` à jour. |

- **Types** : `feat` · `fix` · `chore` · `docs` · `test` · `refactor` · `data`.
- **Nom** : `type/ID-slug`, avec un slug en anglais en minuscules séparées par des tirets : `feat/EVT-09-image-gallery-form`, `fix/UI-07-responsive-data-table`. Une correction mineure sans carte peut omettre l'identifiant : `fix/event-mine-unauthorized-status`.
- **Branches empilées** : si une tâche dépend d'une branche pas encore fusionnée, on la crée à partir de cette branche et on ouvre les Pull Requests dans l'ordre.

## 5. Messages de commit

Format **Conventional Commits**, en anglais, à l'impératif : `type(scope): description`.

```
feat(evt): add event images component
fix(security): require authentication for event mine route
```

- **Titre seul** : pas de description ni de corps de message.
- **Pas de numéro de carte dans le commit** : il est dans le nom de la branche.
- `scope` facultatif : le domaine touché (`evt`, `ui`, `sec`, `anon`, `ops`…).
- Types : `feat` · `fix` · `chore` · `docs` · `test` · `refactor` · `style` · `data`.
- Des commits atomiques : un commit = un pas cohérent, pas un gros commit fourre-tout.

## 6. Cycle d'une tâche

| # | Étape |
|---|---|
| 1 | Prendre une carte de la colonne **Sprint**, la passer **En cours**, se l'assigner. |
| 2 | Créer la branche `type/ID-slug` depuis `dev` à jour. |
| 3 | Développer, avec ses tests, en commits atomiques. |
| 4 | Avant de pousser : les tests du back (`./mvnw test`) et du front (`npx ng test --watch=false`) passent. |
| 5 | Ouvrir une Pull Request **vers `dev`**, avec un titre et une description courts ; la carte passe **En revue**. |
| 6 | L'intégration continue du front se lance seule sur la Pull Request : ESLint, tests et build. Elle doit être verte. |
| 7 | Une **autre personne** relit et fusionne dans `dev` ; la carte passe **Terminé**. |

## 7. Checklist d'une carte « Back »

- [ ] Entité JPA (enum, `@Column`, constructeur sans argument, relations propriétaire et inverse)
- [ ] Repository (`JpaRepository`, requêtes dérivées, `@EntityGraph` si N+1)
- [ ] DTO (`record`) de requête et de réponse
- [ ] Mapper (`@Component`, `toDto` / `toEntity`)
- [ ] Service (`@Transactional`, validation manuelle qui lève `FunctionalException`, logique métier)
- [ ] Interface de documentation Swagger et contrôleur
- [ ] Tests JUnit (service pour les règles métier, intégration pour la route)
- [ ] **Sécurité** : rôle vérifié (`@Secured`), propriété de la ressource vérifiée dans le service, aucune donnée sensible dans les logs, secrets uniquement dans les variables d'environnement

## 8. Ce qui a changé depuis la fiche de départ

| Sujet | Fiche d'origine | Pratique actuelle |
|---|---|---|
| Branche de base et cible des Pull Requests | `main` | **`dev`** ; `main` reçoit `dev` ponctuellement |
| Numéro de carte | obligatoire dans le message de commit | dans le **nom de la branche**, pas dans le commit |
| Corps du message de commit | non précisé | **titre seul** |
| Colonnes Trello | Backlog · À faire (sprint) · En cours · En revue · Terminé, Bloqué facultatif | A faire · **Sprint** · En cours · En revue · Terminé · Bloqué · **Optionnel** |
| Étiquettes de piste | Back · Front · Sécurité · Tests · Config/DevOps · Doc | Back · Front · **Security** · **Test** · Config/Devops · **Conception** |
| Étiquettes de priorité | P1 · P2 · P3 | High · Medium · Low priority |
| Préfixes de carte | `INSC` | **`REG`** ; ajout de `UI`, `DOC` |
| Exemple de nom de branche | slug en français | slug en **anglais** |
| Types de branche | feat · fix · test · refactor · chore · docs | + **`data`** (jeux de données) |
| Intégration continue | non prévue | **CI du front** sur chaque Pull Request vers `dev` |

## 9. Règles de sécurité au quotidien

- Aucun secret dans le dépôt : mots de passe, clés d'API et `JWT_SECRET` vont dans `backend/.env.properties` (ignoré par Git) ou dans les variables d'environnement de l'hébergeur.
- Une nouvelle route est **protégée par défaut** ; la rendre publique se décide en équipe (liste blanche de `SecurityConfig`).
- Avant une livraison et à chaque mise à jour de dépendance : contrôle des dépendances (voir la section « Sécurité des dépendances » du README).
- Les mesures de sécurité du projet sont décrites dans la section « Règles de sécurité » du Dossier de conception.
