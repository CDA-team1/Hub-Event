[← Sommaire](README.md)

# 04 — Clubs

Tickets Trello : CLUB-01 à CLUB-03.
Cas d'utilisation : CU24 (gérer les clubs).
Écrans (maquettes) : 23 (liste des clubs affiliés), 24 (ajout/modification d'un club).

## Arbre des composants

```
ClubsListPage                     features/clubs · entrée : — (liste publique)
├── Pagination                    shared/ui
└── ClubCard × n                  shared/ui · entrée : club

ClubFormPage                      features/clubs (admin) · entrée : id (optionnel)
└── ClubForm                      features/clubs · sortie : submitted(ClubDto)
    └── MembersPicker             features/clubs · entrées : members, selected · sortie : selectionChanged
```
