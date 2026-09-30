[← Sommaire](README.md)

# 06 — Anonymisation et mentions légales

Tickets Trello : ANON-01 à ANON-03, RGPD-01 à RGPD-03.
Cas d'utilisation : CU7 (consulter la politique RGPD), CU8 (consulter les CGU), CU14
(demander l'anonymisation), CU28 (consulter les demandes), CU29 (valider une
anonymisation).
Écrans (maquettes) : 08 (demande d'anonymisation), 20 (demandes d'anonymisation, admin),
22 (saisie RGPD & CGU, admin). Les pages publiques CGU/RGPD n'ont pas de maquette dédiée
numérotée mais sont citées dans le CdC (consultation via fichier/page depuis le pied de
page).

## Arbre des composants

```
AnonymizationRequestPage           features/privacy · entrée : — (utilisateur connecté)
└── (bouton + texte d'explication, pas de formulaire)

AnonymizationAdminPage             features/privacy (admin) · entrée : query params (page)
├── Pagination                     shared/ui
└── AccountCard × n                shared/ui · sortie : validateClicked(requestId)

CguPage / RgpdPage                 features/privacy · entrée : — (contenu public)

LegalDocumentFormPage               features/privacy (admin) · entrée : type (CGU | RGPD)
└── RichTextEditor ou Textarea      features/privacy · sortie : submitted(content)
```
