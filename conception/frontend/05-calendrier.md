[← Sommaire](README.md)

# 05 — Calendrier

Tickets Trello : CAL-01, CAL-02.
Cas d'utilisation : CU11 (consulter mon calendrier).
Écrans (maquettes) : 09 (mon calendrier d'événements).

## Arbre des composants

```
CalendarPage                      features/calendar · entrée : — (période, gérée en interne)
├── DateRangePicker                features/calendar · sortie : rangeChanged(from, to)
├── EventCard × n                  shared/ui (événements inscrits, triés chronologiquement)
└── EmptyState                     shared/ui (aucun événement sur la période)
```

Le calendrier ne montre que les inscriptions au statut « inscrit » (`REGISTERED`) : une
personne en liste d'attente n'y figure pas tant qu'elle n'a pas été promue.
