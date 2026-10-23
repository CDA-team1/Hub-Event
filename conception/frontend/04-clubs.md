[← Sommaire](README.md)

# 04 — Clubs

Tickets Trello : CLUB-01 à CLUB-03.
Cas d'utilisation : CU24 (gérer les clubs).
Écrans (maquettes) : 23 (liste des clubs affiliés), 24 (ajout/modification d'un club).

## Arbre des composants

```
ClubsListPage                     features/clubs (admin) · entrée : — (CdC p.10, réservé à l'administrateur)
├── Pagination                    shared/ui
└── DataTable                     shared/ui · colonnes nom/catégorie/adresse/email/téléphone

ClubFormPage                      features/clubs (admin) · entrée : id (optionnel)
└── ClubForm                      features/clubs · sortie : submitted(ClubDto)
```

Pas de gestion des membres affiliés dans ce formulaire (CdC p.11, `ClubDto` back) : l'affiliation
est une fonctionnalité séparée (CU26/27), pas le CRUD club.
