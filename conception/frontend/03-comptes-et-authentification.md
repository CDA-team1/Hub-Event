[← Sommaire](README.md)

# 03 — Comptes, authentification et suspension

Tickets Trello : AUTH-01 à AUTH-03, CPT-01 à CPT-06, SUSP-01.
Cas d'utilisation : CU4 (connexion), CU5 (créer un compte non affilié), CU6 (activer son
compte), CU13 (modifier mon compte), CU15 (déconnexion), CU25 (gérer les comptes), CU26/27
(affiliations), CU30 (suspendre).
Écrans (maquettes) : 04 (connexion), 05 (création de compte), 06 (confirmation de
compte), 07 (modifier mon compte), 11 (confirmation de compte créé par un admin), 16
(liste des comptes), 17/18/19 (création/modification compte admin/organisateur/membre),
21 (suspension).

## Arbre des composants

```
LoginPage                          features/auth · sortie : (aucune, navigue après succès)
└── LoginForm                      features/auth · sortie : submitted(credentials)

SignupPage                         features/account
└── SignupForm                     features/account · sortie : submitted(CreateUserRequest)

ActivateAccountPage                features/account · entrée : token (query param)
ConfirmAccountCreationPage         features/account · entrée : token (query param)
└── ConfirmAccountForm             features/account · sortie : submitted(temp, new, confirm)

MyAccountPage                      features/account · entrée : — (utilisateur connecté)
└── AccountForm                    features/account · sortie : submitted(UpdateUserRequest)

UsersListPage                      features/account (admin) · entrée : query params (rôle, page)
├── Pagination                     shared/ui
└── AccountCard × n                shared/ui · sortie : suspendClicked(id)

UserFormPage                       features/account (admin) · entrée : id (optionnel), rôle cible
└── UserForm                       features/account · sortie : submitted(AdminUserRequest)
    └── ClubAffiliationPicker      features/account · entrées : clubs, selected · sortie : selectionChanged

SuspendUserForm                    features/account (admin) · sortie : submitted(reason, endDate | null)
```

Décisions à commenter :
- `UserForm` est un seul composant paramétré par le rôle cible (membre/organisateur/admin),
  pas trois composants séparés : les champs varient (affiliations pour un membre ou un
  organisateur, aucune pour un admin) mais la structure et la validation de base sont
  identiques — même logique que `EventForm` en 01.
- La session (`Auth`, dans `core/auth`) est le seul détenteur de l'état de connexion :
  `Header`, les gardes de route et `MyAccountPage` la consultent, aucun ne la duplique.
