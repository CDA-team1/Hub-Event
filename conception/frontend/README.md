# Conception front — Hub évènementiel

Ce dossier applique la partie « découpage en arbre de composants » de la phase 04 du
cours Angular (« Conception : découpage et user stories ») au projet Hub évènementiel. Il
ne contient pas de code : il fixe les décisions qui guideront l'implémentation (Trello,
phases SOCLE/UI/domaines).

Sources utilisées :
- Cahier des charges et Dossier de spécifications générales (`../backend/`).
- Les 24 maquettes low-fi desktop + 24 mobile (`../backend/Hub Évènementiel low fi
  desktop|mobile/`).
- L'API back existante (contrôleurs, DTO, règles métier réellement implémentées).

## Méthode

Pour chaque écran on répond aux quatre questions de la phase 04 :
1. Qu'est-ce qui se répète ? → un composant, alimenté par une entrée.
2. Qu'est-ce qui revient sur plusieurs écrans ? → `shared/`.
   Un composant utilisé par une seule page reste dans le dossier de cette page.
3. Qui détient la donnée ? → le composant le plus haut qui en a besoin, ou un service
   `core/` si plusieurs écrans la partagent.
4. Qui décide ? → un composant d'affichage n'émet que des événements ; c'est toujours une
   page (`features/`) qui décide et appelle les services.

Chaque domaine ci-dessous fournit l'arbre des composants de ses écrans (page vs
affichage, dossier, entrées/sorties) et les décisions de conception associées.

## Sommaire

- [00 — Arbre des composants (vue globale)](00-arbre-composants.md)
- [01 — Événements et recherche](01-evenements.md) — EVT, SEARCH
- [02 — Inscriptions et commentaires](02-inscriptions-et-commentaires.md) — REG, COM
- [03 — Comptes, authentification et suspension](03-comptes-et-authentification.md) — AUTH, CPT, SUSP
- [04 — Clubs](04-clubs.md) — CLUB
- [05 — Calendrier](05-calendrier.md) — CAL
- [06 — Anonymisation et mentions légales](06-anonymisation-et-mentions-legales.md) — ANON, RGPD

Chaque fichier référence les tickets Trello correspondants (mêmes préfixes que le back :
EVT, REG, COM, CPT, AUTH, CLUB, CAL, ANON, RGPD, SUSP, SEARCH) ainsi que les numéros de cas
d'utilisation (CU) du Dossier de spécifications générales.
