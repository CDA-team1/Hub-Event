[← Sommaire](README.md)

# 01 — Événements et recherche

Tickets Trello : EVT-01 à EVT-10, SEARCH-01.
Cas d'utilisation : CU2 (rechercher), CU3 (consulter le détail), CU16 à CU23 (créer,
modifier, publier, changer de statut, annuler, supprimer, gérer la galerie).
Écrans (maquettes) : 01 (accueil), 02 (recherche), 03 (détail visiteur), 10 (détail
connecté), 12 (mes événements), 13 (création/modification), 14 (suppression/annulation).

## Arbre des composants

```
EventsHomePage                    features/events · entrée : — (données via EventApi)
├── Carousel × 3 (une par catégorie)     shared/ui
│   └── EventCard × n                    shared/ui · entrées : event, showActions
└── EmptyState                           shared/ui (si aucun événement publié)

EventSearchPage                   features/events · entrée : query params (critères)
├── SearchForm                    features/events · sortie : criteriaChanged
├── EventCard × n                 shared/ui
└── EmptyState

EventDetailPage                   features/events · entrée : id (paramètre de route)
├── StatusBadge, CategoryBadge    shared/ui · entrée : status / category
├── ImageGallery                  features/events · entrée : images · sortie : imageAdded, imageRemoved (organisateur propriétaire uniquement)
├── RegistrationPanel             features/events · entrées : event, myRegistration · sorties : registerClicked, unregisterClicked — voir 02
├── CommentList / CommentForm     shared/ui · voir 02
└── OwnerActions                  features/events · entrée : event · sorties : editClicked, publishClicked, finishClicked, cancelClicked, deleteClicked (visible seulement si organisateur propriétaire)

MyEventsPage                      features/events · entrée : — (événements de l'organisateur connecté)
├── StatusBadge × n                shared/ui
└── EventCard × n (variante compacte)   shared/ui

EventFormPage                     features/events · entrée : id (optionnel, absent = création)
└── EventForm                     features/events · sortie : submitted(CreateEventRequest | UpdateEventRequest)
```

Décisions à commenter :
- `EventDetailPage` est un seul composant pour les maquettes 03 et 10 : la version
  « visiteur » n'est pas un composant différent, c'est la même page dont certains blocs
  (`RegistrationPanel`, `CommentForm`, `OwnerActions`) ne s'affichent pas selon la session.
  Cela évite de dupliquer la logique d'affichage du détail.
- `OwnerActions` ne décide de rien : chaque clic appelle `EventApi`, et c'est la page qui
  gère l'erreur métier renvoyée par le back (ex. « ne peut pas être annulé dans son état
  actuel »).
- Le filtre de `EventSearchPage` vit dans les paramètres d'URL, jamais dans un signal
  local : un lien de recherche est partageable.
