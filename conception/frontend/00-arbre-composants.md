# 00 — Arbre des composants (vue globale)

## Coquille de l'application

```
App                              en-tête, navigation, <router-outlet>
├── Header                       shared/ui · entrées : session (connecté ?, rôle)
├── Footer                       shared/ui
└── <router-outlet>
    ├── EventsHomePage           features/events         — voir 01
    ├── EventSearchPage          features/events         — voir 01
    ├── EventDetailPage          features/events         — voir 01, 02
    ├── MyEventsPage             features/events         — voir 01
    ├── EventFormPage            features/events         — voir 01
    ├── LoginPage                features/auth           — voir 03
    ├── SignupPage                features/account        — voir 03
    ├── ActivateAccountPage       features/account        — voir 03
    ├── ConfirmAccountPage        features/account        — voir 03
    ├── MyAccountPage             features/account        — voir 03
    ├── UsersListPage             features/account        — voir 03
    ├── UserFormPage              features/account        — voir 03
    ├── ClubsListPage             features/clubs          — voir 04
    ├── ClubFormPage              features/clubs          — voir 04
    ├── CalendarPage              features/calendar       — voir 05
    ├── AnonymizationRequestPage  features/privacy        — voir 06
    ├── AnonymizationAdminPage    features/privacy        — voir 06
    ├── CguPage / RgpdPage        features/privacy        — voir 06
    ├── LegalDocumentFormPage     features/privacy        — voir 06
    └── NotFoundPage              features/not-found
```

Chaque `*Page` est chargée avec `loadComponent` (lazy) et porte un `title` de route. Les
pages qui modifient une ressource existante (formulaire d'événement, de compte, de club,
de document légal) réutilisent le même composant en création et en modification — décision
alignée avec pokedev (`create-dev-page` vs `edit-dev-page` y sont séparés, mais nos
formulaires back sont déjà unifiés côté DTO : on garde un seul composant, paramétré par la
présence ou non d'un `id` dans la route).

## Qui détient l'état partagé

| État | Détenteur | Pourquoi |
|---|---|---|
| Session (JWT, rôle, email) | Service `Auth` (`core/auth`) | Utilisé par le header, les gardes de route, l'intercepteur HTTP et plusieurs pages : aucune page ne le détient seule. Persisté (`persistedSignal`, façon pokedev) pour survivre à un rechargement. |
| Événements | Service `EventApi` (`core/events`) + `httpResource` | Chaque page reste une simple consommatrice ; pas de cache partagé nécessaire au-delà de ce que `httpResource` offre déjà. |
| Filtre de recherche | Paramètres d'URL (query params) | Lien partageable, cohérent avec le choix déjà fait pour le filtre par type dans pokedev. |
| Chargement HTTP global | Service `Loading` (`core/http`) | Un indicateur partagé par toute l'application, mis à jour par un intercepteur. |

## Composants partagés (`shared/`)

| Composant | Dossier | Entrées | Sorties | Réutilisé par |
|---|---|---|---|---|
| `EventCard` | `shared/ui` | `event`, `showActions` | `registerToggled(id)` | accueil, recherche, mes événements |
| `DataTable` | `shared/ui` | `columns`, `rows`, `trackBy`, `sort` | `sortChanged(key)` | liste des clubs, des comptes, des demandes d'anonymisation, calendrier, mes évènements, inscrits (maquettes 09, 12, 15, 20, 23 : tableaux, pas de cartes) |
| `ActionButton` | `shared/ui` | `icon`, `label`, `level`, `routerLink` (optionnel) | `triggered` | colonne « Actions » de chaque `DataTable` |
| `StatusBadge` | `shared/ui` | `status` (EventStatus) | — | toutes les pages événement |
| `CategoryBadge` | `shared/ui` | `category` | — | événements, clubs |
| `RoleBadge` | `shared/ui` | `role` | — | comptes |
| `EmptyState` | `shared/ui` | contenu projeté | — | toutes les listes |
| `LoadingIndicator` | `shared/ui` | — (lit `Loading` directement) | — | racine de l'app |
| `ErrorState` | `shared/ui` | `message`, `retry` (optionnel) | `retryClicked` | toutes les pages avec `httpResource` |
| `ConfirmDialog` (service) | `core/dialog` | — | — (retourne une `Promise<boolean>`) | annulation/suppression d'événement, fin d'affiliation club, validation anonymisation |
| `Pagination` | `shared/ui` | `page`, `totalPages` | `pageChanged(n)` | comptes, clubs, demandes d'anonymisation |

Un composant utilisé par **une seule page** n'est pas dans `shared/` : il est rangé dans le
dossier de cette page. Pour les événements : `Carousel` dans `events-home-page`, `SearchForm`
dans `event-search-page`, `RegistrationPanel`, `OwnerActions`, `ImageGallery` et
`CommentList` dans `event-detail-page`, et `CommentForm` dans `comment-list`.

Deux décisions à commenter, dans l'esprit de la phase 04 :
- `EventCard` ne sait pas si l'utilisateur est inscrit : elle reçoit l'état déjà calculé et
  émet une intention (`registerToggled`), c'est la page qui appelle `EventApi`/`RegistrationApi`
  et décide de la suite (place restante, liste d'attente, erreur).
- `ConfirmDialog` est un **service**, pas un composant placé dans le template de chaque
  page : cela évite de dupliquer la modale sur les 4 écrans qui en ont besoin (annulation
  d'événement, suppression, fin d'affiliation, validation d'anonymisation).

## Détail par domaine

Le détail page par page (entrées/sorties précises) est dans les fichiers numérotés du
sommaire — voir [README](README.md).
