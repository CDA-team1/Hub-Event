[← Sommaire](README.md)

# 02 — Inscriptions et commentaires

Tickets Trello : REG-01 à REG-04, COM-01, COM-02.
Cas d'utilisation : CU9 (s'inscrire), CU10 (se désinscrire), CU12 (commenter), CU22
(annuler l'inscription d'un membre).
Écrans (maquettes) : 10 (détail connecté, inscription/commentaire), 15 (désinscription
d'un membre par l'organisateur).

Ces deux domaines vivent tous les deux sur `EventDetailPage` (voir 01) : ils n'ont pas de
page dédiée, seulement des blocs de cette page et une page de gestion pour l'organisateur.

## Arbre des composants

```
RegistrationPanel                 features/events/event-detail-page · entrées : event, myRegistration
│                                  · sorties : registerClicked, unregisterClicked
└── (pas de sous-composant : juste un bouton + l'affichage du statut/de la position en liste d'attente)

EventRegistrationsPage             features/events · entrée : id événement (route)
├── DataTable                      shared/ui · inscrits, action « Désinscrire » (ActionButton)
└── formulaire Motif (intégré)     affiché sous le tableau au clic sur « Désinscrire » (maquette 15)

CommentList                        features/events/event-detail-page · entrée : comments
CommentForm                        features/events/event-detail-page/comment-list · sortie : submitted(content)
```

Décision à commenter : `RegistrationPanel` ne connaît pas les règles de statut (places
restantes, chevauchement horaire) : elle affiche ce que `EventDetailPage` lui donne et
émet une intention. C'est la page qui appelle `RegistrationApi.register()` et interprète
l'erreur métier renvoyée par le back (déjà inscrit, événement non ouvert, créneau qui se
chevauche).
