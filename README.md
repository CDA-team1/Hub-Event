# 🎪 Hub-Event : gestion d'un hub évènementiel

Application web de gestion d'évènements pour une **fédération associative régionale** qui regroupe plusieurs clubs (sport, culture, loisirs). Les organisateurs publient des évènements, les membres s'y inscrivent et les organisateurs gèrent les participants.

**Stack** : ☕ Spring Boot 4.1 / Java 21 (back) · 🅰️ Angular 22 (front) · 🐬 MySQL.

## 📑 Sommaire

- [✨ Fonctionnalités](#-fonctionnalités)
- [🧰 Prérequis](#-prérequis)
- [🚀 Démarrage rapide](#-démarrage-rapide)
- [👤 Comptes de démo](#-comptes-de-démo)
- [🧪 Tests et build](#-tests-et-build)
- [📁 Structure du dépôt](#-structure-du-dépôt)
- [🧱 Architecture](#-architecture)
- [📚 Documentation](#-documentation)
- [🤝 Contribuer](#-contribuer)
- [🔧 Dépannage](#-dépannage)
- [👥 Équipe](#-équipe)

## ✨ Fonctionnalités

| Profil | Ce qu'il peut faire |
|---|---|
| 👀 **Visiteur** (non connecté) | Consulter et rechercher les évènements, lire les CGU et la politique RGPD, se connecter, créer un compte non affilié (activation par e-mail). |
| 🙋 **Membre** (affilié ou non) | S'inscrire et se désinscrire d'un évènement, suivre son calendrier (export Excel), commenter, télécharger la fiche PDF d'un évènement, demander l'anonymisation de ses données. |
| 🎯 **Organisateur** | Créer, modifier, publier, terminer, annuler ou supprimer ses évènements, gérer la galerie de photos, consulter les inscrits et désinscrire un membre avec un motif. |
| 🔑 **Administrateur** | Gérer les clubs et les comptes (création, modification, suppression d'un compte sans historique), suspendre un compte, traiter les demandes d'anonymisation, rédiger les CGU et la politique RGPD. |

Règles métier principales :

- 🔄 Cycle de vie d'un évènement : **Brouillon → Publié → Annulé ou Terminé**. Catégories figées : culture, loisirs, sport. Un évènement annulé reste consultable.
- 💶 Deux tarifs par évènement : **affilié** et **non affilié**.
- ⏳ Quand l'évènement est complet, les inscriptions passent en **liste d'attente** ; une désinscription promeut automatiquement le premier en attente. Les inscriptions sont verrouillées côté serveur pour éviter la surréservation.
- 🔐 Mot de passe fort (12 caractères, 4 types), compte inactif tant que le lien d'activation n'est pas cliqué.
- 🔒 Anonymisation RGPD : un évènement annulé automatiquement prévient ses inscrits et libère les places.
- 📷 Sans photo, un évènement affiche une illustration par défaut selon sa catégorie.

## 🧰 Prérequis

| Outil | Version |
|---|---|
| ☕ Java (JDK) | **21** |
| 🔨 Maven | inclus via le wrapper (`mvnw`, Maven 3.9), rien à installer |
| 🟢 Node.js | `^22.22.3`, `^24.15.0` ou `>=26` |
| 📦 npm | 11 |
| 🐬 MySQL | **8.x** |

Pour un fonctionnement complet, il faut aussi : un compte **Gmail avec mot de passe d'application** pour l'envoi d'e-mails (indispensable pour créer un compte avec activation par e-mail), et une **clé API gratuite imgbb** pour les photos d'évènements (facultative en développement).

## 🚀 Démarrage rapide

### 📥 1. Cloner le dépôt

```bash
git clone https://github.com/CDA-team1/Hub-Event.git
cd Hub-Event
```

### 💾 2. Créer la base MySQL

Le schéma est créé automatiquement au démarrage, mais **la base doit exister** :

```sql
CREATE DATABASE ghe_db_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> 💡 En développement, la base est **recréée à chaque démarrage** du back (`create-drop`) et rechargée avec les données de démo.

### 🔑 3. Configurer les secrets du back

Les secrets ne doivent jamais être versionnés. Depuis le dossier `backend`, copie le fichier d'exemple puis renseigne les valeurs :

```bash
cd backend
cp .env.properties.example .env.properties
```

| Variable | Rôle | Exemple en développement |
|---|---|---|
| `DB_USER` / `DB_PASSWORD` | Accès à MySQL | ton utilisateur MySQL |
| `JWT_SECRET` | Clé de signature des jetons, **encodée en Base64** (32 octets minimum) | voir ci-dessous |
| `APP_FRONTEND_URL` | URL du front (liens des e-mails : activation, confirmation de compte et de mot de passe) | `http://localhost:4200` |
| `MAIL_HOST` / `MAIL_PORT` | Serveur SMTP | `smtp.gmail.com` / `587` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Compte e-mail et [mot de passe d'application](https://myaccount.google.com/apppasswords) | ton compte Gmail |
| `IMGBB_API_KEY` | Clé de l'hébergeur d'images, créée sur [api.imgbb.com](https://api.imgbb.com/) | ta clé |

Générer une clé `JWT_SECRET` :

```bash
openssl rand -base64 32
```

Ou, sous PowerShell :

```powershell
$b = New-Object byte[] 32; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

### 🌱 4. Lancer le back

Toujours depuis `backend` (le fichier `.env.properties` est lu dans le dossier courant) :

```bash
./mvnw spring-boot:run
```

Sous Windows (PowerShell) : `.\mvnw.cmd spring-boot:run`.

L'API écoute sur **http://localhost:8080/api**. Le profil `dev` est actif par défaut.

### 🎨 5. Lancer le front

Dans un second terminal :

```bash
cd frontend
npm install
npm start
```

L'application est disponible sur **http://localhost:4200**. Le serveur de développement redirige tous les appels `/api/*` vers le back (`proxy.conf.json`).

## 👤 Comptes de démo

Les données de démo (41 évènements, 5 clubs, des inscriptions, des commentaires, des photos) sont chargées au démarrage en `dev`. **Tous les comptes ont le même mot de passe : `Password123!`**

| Compte | Profil |
|---|---|
| `member.demo@ghe.local` | 🙋 Membre actif, affilié à 2 clubs |
| `organizer.demo@ghe.local` | 🎯 Organisateur (propriétaire de la plupart des évènements) |
| `admin.demo@ghe.local` | 🔑 Administrateur |
| `nonaffilie.demo@ghe.local` | 🙋 Membre actif non affilié |
| `pending.demo@ghe.local` | ⏳ Compte inactif, en attente d'activation |
| `suspended.temp.demo@ghe.local` | 🚫 Compte suspendu temporairement |

La liste complète des 18 comptes (un par état à tester) est décrite en tête de [`backend/src/main/resources/data.sql`](backend/src/main/resources/data.sql).

## 🧪 Tests et build

**Back** (depuis `backend`) :

```bash
./mvnw test                          # tests JUnit (base H2 en mémoire)
./mvnw -DskipTests clean package     # construit le jar
```

**Front** (depuis `frontend`) :

```bash
npm test                             # tests Vitest en mode surveillance
npx ng test --watch=false            # une seule exécution
npm run build                        # build de production dans dist/
```

Les profils Maven `dev` (par défaut), `test` et `prod` sélectionnent le profil Spring correspondant. En `prod`, le schéma n'est pas généré (`ddl-auto=validate`) et les données de démo ne sont pas chargées.

## 📁 Structure du dépôt

```text
Hub-Event/
├── backend/                  API Spring Boot
│   ├── src/main/java/fr/CDA/GHE/
│   │   ├── controller/       endpoints REST (+ interfaces *ControllerDoc pour Swagger)
│   │   ├── service/          logique métier
│   │   ├── repository/       accès aux données (Spring Data JPA)
│   │   ├── entity/           entités JPA
│   │   ├── dto/  mapper/     objets d'échange et conversions
│   │   ├── security/  config/ JWT, règles d'accès, configuration
│   │   └── exception/        gestion centralisée des erreurs
│   ├── src/main/resources/   configuration par profil, data.sql (démo)
│   ├── docs/database/        schema.sql (structure SQL documentée)
│   └── .env.properties.example
├── frontend/                 application Angular
│   └── src/app/
│       ├── core/             accès API, authentification, intercepteurs
│       ├── domain/           modèles et règles métier pures
│       ├── features/         une page = un dossier (ses composants dedans)
│       └── shared/ui/        composants réutilisables (tableau, carte, badge…)
└── conception/               cahier des charges, spécifications, diagrammes, maquettes
```

## 🧱 Architecture

- **Back** : architecture en couches `controller → service → repository`, DTO et mappers, authentification **JWT sans session**, mots de passe hachés en BCrypt, droits par rôle avec `@Secured`.
- **E-mails** : les e-mails indispensables (activation, création de compte par un admin, confirmation de changement de mot de passe) sont envoyés immédiatement ; une panne SMTP fait échouer l'action. Les notifications (liste d'attente, annulations, suspension) partent en tâche de fond (`@Async`) : une panne est seulement journalisée.
- **Front** : composants autonomes, signaux, un dossier par page. Le jeton est conservé dans le `sessionStorage` ; les routes sensibles sont protégées par des gardes de rôle.
- **Services externes** : SMTP (e-mails) et imgbb (hébergement des photos).

## 📚 Documentation

- [`conception/`](conception/) : cahier des charges, dossier de spécifications, dossier de conception, diagrammes (classes, cas d'usage, entités-relations) et maquettes desktop et mobile.
- [`conception/frontend/`](conception/frontend/) : notes de conception du front (arbre des composants, un fichier par domaine).
- **API** : documentation OpenAPI via Swagger UI sur `http://localhost:8080/api/swagger-ui/index.html`.
- [`backend/docs/database/schema.sql`](backend/docs/database/schema.sql) : structure SQL de la base (MySQL 8).

## 🤝 Contribuer

- 🌿 **Branches** : `dev` est la branche d'intégration. Une branche par carte de travail, nommée `type/CARTE-description` (ex. `feat/EVT-09-image-gallery-form`, `fix/UI-07-responsive-data-table`).
- 📝 **Commits** : `type(scope): description` courte, en anglais, sans numéro de carte (ex. `feat(evt): add event images component`). Types : `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `style`.
- 🔀 **Pull requests** vers `dev`, avec un titre et une description courts. Avant de pousser : les tests du back et du front doivent passer.
- 🎨 **Formatage du front** : Prettier (`frontend/.prettierrc`).

## 🔧 Dépannage

| Symptôme | Cause et solution |
|---|---|
| ❌ `Unknown database 'ghe_db_dev'` au démarrage du back | La base n'existe pas : voir l'étape 2. |
| ❌ `Could not resolve placeholder '...'` | Une variable manque dans `backend/.env.properties` (ou le back est lancé hors du dossier `backend`). |
| ❌ Les tests du back échouent avec `APP_FRONTEND_URL` | Le profil de test lit `.env.properties` pour cette valeur : vérifie que le fichier existe et contient `APP_FRONTEND_URL`. |
| 🔒 Swagger UI répond 401 | L'accès à la documentation de l'API nécessite pour l'instant un jeton JWT. |
| 🟢 Erreur de version de Node au `npm install` | Utilise Node 22.22+, 24.15+ ou 26+ (voir Prérequis). |
| 📧 La création d'un compte échoue (délai de 5 s puis erreur) | L'e-mail d'activation est indispensable : vérifie les variables `MAIL_*` et le mot de passe d'application Gmail. En développement, utilise les comptes de démo déjà actifs. |
| 📭 Les e-mails de notification n'arrivent pas | Ils sont envoyés en tâche de fond : une panne SMTP est seulement journalisée dans les logs du back, sans bloquer l'action. |

## 👥 Équipe

Projet réalisé par l'équipe **CDA-team1** : Anthony Gargoua, Damien Castello et Garance Guinet. 💪
