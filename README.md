# 🎪 Hub-Event : gestion d'un hub évènementiel

Application web de gestion d'évènements pour une **fédération associative régionale** qui regroupe plusieurs clubs (sport, culture, loisirs). Les organisateurs publient des évènements, les membres s'y inscrivent et les organisateurs gèrent les participants.

**Stack** : ☕ Spring Boot 4.1 / Java 21 (back) · 🅰️ Angular 22 (front) · 🐬 MySQL.

## 📑 Sommaire

- [✨ Fonctionnalités](#-fonctionnalités)
- [🧰 Prérequis](#-prérequis)
- [🚀 Démarrage rapide](#-démarrage-rapide)
- [👤 Comptes de démo](#-comptes-de-démo)
- [🧪 Tests et build](#-tests-et-build)
- [🔒 Sécurité des dépendances](#-sécurité-des-dépendances)
- [📜 Journaux (logs)](#-journaux-logs)
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

## 🔒 Sécurité des dépendances

Deux contrôles à lancer avant une livraison et à chaque mise à jour de dépendance.

**Back** (depuis `backend`) : [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/) compare les bibliothèques aux CVE connues. Il a besoin d'une clé d'API NVD, gratuite : [nvd.nist.gov/developers/request-an-api-key](https://nvd.nist.gov/developers/request-an-api-key). Sans elle, le plugin refuse de démarrer.

1. Déclarer la clé dans le terminal, selon le shell (valable pour ce terminal seulement). La clé ne se met **ni dans le dépôt ni dans `.env.properties`** : ce fichier n'est lu que par l'application, pas par Maven.

   | Terminal | Commande |
   |---|---|
   | Linux, macOS, Git Bash (bash, zsh) | `export NVD_API_KEY=ta_cle` |
   | PowerShell | `$env:NVD_API_KEY="ta_cle"` |
   | Invite de commandes (cmd) | `set NVD_API_KEY=ta_cle` |

   Pour ne pas la retaper à chaque terminal : ajouter la ligne `export` dans `~/.bashrc` (bash) ou `~/.zshrc` (zsh), ou lancer `setx NVD_API_KEY ta_cle` sous Windows puis rouvrir le terminal. Pour savoir quel shell on utilise : `echo $SHELL`.

2. Lancer le contrôle :

   ```bash
   ./mvnw org.owasp:dependency-check-maven:check      # sous Windows : .\mvnw.cmd ...
   ```

- Le premier lancement télécharge toute la base NVD (plus de 400 000 enregistrements), les suivants sont rapides. Selon l'état du serveur NVD, cela prend de 20 minutes à plusieurs heures : à lancer à un moment calme (soir, week-end). Le rapport est dans `backend/target/dependency-check-report.html`.
- Si le téléchargement s'enlise (messages `Retrying request ...` en boucle), interrompre avec Ctrl+C et relancer plus tard avec des pages plus petites : `./mvnw org.owasp:dependency-check-maven:check -DnvdApiResultsPerPage=500 -DnvdMaxRetryCount=20`.
- Le contrôle échoue à partir d'une vulnérabilité de gravité haute (CVSS 7). Une fausse alerte se déclare dans `backend/dependency-check-suppressions.xml`, avec sa justification et une date de fin.

**Front** (depuis `frontend`) :

```bash
npm run audit:prod     # dépendances livrées aux utilisateurs : doit rester à 0
npm audit              # tout, y compris les outils de développement
```

### Analyse du 22/10/2026

Le back a été analysé avec la base ouverte [OSV.dev](https://osv.dev) (avis de sécurité par version exacte). Le scan OWASP, lui, n'a pas pu aller au bout ce jour-là : le serveur NVD répondait trop lentement pour télécharger sa base complète (voir ci-dessus). À relancer à un moment calme, puis à comparer avec ce tableau.

| Dépendance | Problème | Concerne l'application ? | Décision |
|---|---|---|---|
| Tomcat 11.0.24 (3 avis critiques) | contournement des authentifications DIGEST et FORM de Tomcat | Non : l'authentification passe par Spring Security et JWT | Corrigé : Spring Boot 4.1.2 (Tomcat 11.0.27) |
| Jackson 3.1.5 et 2.21.5 (7 avis) | déni de service à la lecture de JSON (expressions régulières, nombres, mémoire) | Oui : l'API lit du JSON envoyé par les clients | Corrigé : Jackson 3.1.7 et 2.21.7 (inclus dans Spring Boot 4.1.2) |
| Apache POI 5.3.0 | fichier OOXML piégé à la lecture | Non : l'application n'écrit que des fichiers `.xlsx`, elle n'en lit jamais | Corrigé par prudence : POI 5.4.1 |
| `@angular/router` 22.2.0 | déni de service lors du rendu côté serveur | Non : pas de rendu côté serveur | Corrigé : Angular 22.2.3 |
| `@angular/cli` (SDK MCP), `source-map-js` | outils de développement et de build | Non : jamais dans l'application livrée | Corrigés : Angular CLI 22.2.4, mise à jour du verrouillage |
| `vite` 8.3.0 (dans `@angular/build`) | failles du serveur de développement (`ng serve`) | Non : uniquement sur le poste du développeur | **Accepté** : le correctif imposerait de forcer une version non validée par Angular. À revoir à la prochaine mise à jour d'Angular. |

Résultat : 0 avis sur les 121 dépendances d'exécution du back, `npm run audit:prod` à 0, et `npm audit` complet réduit à `vite` (outil de développement).

## 📜 Journaux (logs)

Le back écrit ses logs sur la **sortie standard** (la console), sans fichier : rien n'est enregistré sur le disque par l'application elle-même.

| Où tourne le back | Où lire les logs |
|---|---|
| En local (`./mvnw spring-boot:run`) | dans le terminal qui l'a lancé |
| Avec Docker Compose | `docker compose logs backend` |
| Sur Render | onglet **Logs** du service (historique limité) |

Pour les conserver plus longtemps, il faut les envoyer ailleurs (fonction de l'hébergeur), ce qui n'est pas en place.

**Événements de sécurité** : ils passent par un journal nommé `SECURITY`, au format `ÉVÉNEMENT : clé=valeur`. On les isole en filtrant sur ce nom :

```bash
docker compose logs backend | grep SECURITY
```

| Événement | Exemple |
|---|---|
| Connexion refusée | `CONNEXION refusée : raison=identifiants invalides email=j***@test.com ip=203.0.113.5` |
| Connexion réussie | `CONNEXION réussie : userId=12 ip=203.0.113.5` |
| Jeton JWT refusé (compte suspendu, inactif, introuvable) | `JETON refusé : raison=compte suspendu userId=12 ip=...` |
| Accès interdit (403) | `ACCÈS REFUSÉ : GET /admin/users userId=12 ip=...` |
| Quota de requêtes dépassé (429) | `LIMITE DE REQUÊTES dépassée : règle=login ip=...` |

**Règle à respecter quand on ajoute un log** : jamais de mot de passe, de JWT, de jeton de lien reçu par email, de lien de suppression d'image, de query string d'URL, ni d'adresse email complète (utiliser l'identifiant du compte, ou `LogMasking.maskEmail`). Des tests (`SensitiveDataInLogsTest` et les tests de journalisation) échouent si l'une de ces données apparaît.

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
- [`CONTRIBUTING.md`](CONTRIBUTING.md) : règles Trello et Git de l'équipe.
- **API** : documentation OpenAPI via Swagger UI sur `http://localhost:8080/api/swagger-ui/index.html` (désactivée avec le profil `prod`).
- [`backend/docs/database/schema.sql`](backend/docs/database/schema.sql) : structure SQL de la base (MySQL 8).

### 🔌 Endpoints de l'API

Tous les endpoints sont préfixés par `/api`. La documentation détaillée est disponible via Swagger UI en environnement de développement.

| Domaine | Méthode | Endpoint |
|---|---|---|
| Authentification | POST | `/auth/login` |
| Authentification | POST | `/auth/signup` |
| Authentification | GET | `/auth/activate` |
| Authentification | GET | `/auth/confirm-password-change` |
| Authentification | POST | `/auth/confirm-account-creation` |
| Authentification | POST | `/auth/logout` |
| Évènements | GET | `/events` |
| Évènements | GET | `/events/mine` |
| Évènements | GET | `/events/{id}` |
| Évènements | GET | `/events/{id}/pdf` |
| Évènements | GET | `/events/search` |
| Évènements | POST | `/events` |
| Évènements | PUT | `/events/{id}` |
| Évènements | DELETE | `/events/{id}` |
| Évènements | POST | `/events/{id}/publish` |
| Évènements | POST | `/events/{id}/status` |
| Évènements | POST | `/events/{id}/cancel` |
| Évènements | POST | `/events/{eventId}/images` |
| Évènements | DELETE | `/events/{eventId}/images/{imageId}` |
| Évènements | PUT | `/events/{eventId}/images/{imageId}/preview` |
| Commentaires | GET | `/events/{id}/comments` |
| Commentaires | POST | `/comments` |
| Inscriptions | POST | `/events/{eventId}/registrations` |
| Inscriptions | GET | `/events/{eventId}/registrations` |
| Inscriptions | DELETE | `/events/{eventId}/registrations/me` |
| Inscriptions | DELETE | `/events/{eventId}/registrations/{userId}` |
| Clubs | GET | `/clubs` |
| Clubs | GET | `/clubs/mine` |
| Clubs | GET | `/clubs/{id}` |
| Clubs | POST | `/clubs` |
| Clubs | PUT | `/clubs/{id}` |
| Clubs | DELETE | `/clubs/{id}` |
| Calendrier | GET | `/calendar` |
| Calendrier | GET | `/calendar/export` |
| Profil utilisateur | GET | `/users/me` |
| Profil utilisateur | PUT | `/users/me` |
| Administration utilisateurs | GET | `/admin/users` |
| Administration utilisateurs | GET | `/admin/users/{id}` |
| Administration utilisateurs | POST | `/admin/users` |
| Administration utilisateurs | PUT | `/admin/users/{id}` |
| Administration utilisateurs | DELETE | `/admin/users/{id}` |
| Administration utilisateurs | PUT | `/admin/users/{id}/clubs` |
| Administration utilisateurs | POST | `/admin/users/{id}/suspension` |
| Anonymisation | POST | `/anonymization-requests` |
| Administration anonymisation | GET | `/admin/anonymization` |
| Administration anonymisation | POST | `/admin/anonymization/{id}/validate` |
| Documents légaux | GET | `/documents/{type}` |
| Documents légaux | GET | `/documents/{type}/pdf` |
| Administration documents | PUT | `/admin/documents/{type}` |

## 🤝 Contribuer

- 🌿 **Branches** : `dev` est la branche d'intégration. Une branche par carte de travail, nommée `type/CARTE-description` (ex. `feat/EVT-09-image-gallery-form`, `fix/UI-07-responsive-data-table`).
- 📝 **Commits** : `type(scope): description (CARTE)` courte, en anglais, avec le numéro de carte (ex. `feat(evt): add event images component (EVT-09)`). Types : `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `style`.
- 🔀 **Pull requests** vers `dev`, avec un titre et une description courts. Avant de pousser : les tests du back et du front doivent passer.
- 🎨 **Formatage du front** : Prettier (`frontend/.prettierrc`).

Les règles complètes (colonnes Trello, étiquettes, identifiants de carte, cycle d'une tâche) sont dans [`CONTRIBUTING.md`](CONTRIBUTING.md).

## 🔧 Dépannage

| Symptôme | Cause et solution |
|---|---|
| ❌ `Unknown database 'ghe_db_dev'` au démarrage du back | La base n'existe pas : voir l'étape 2. |
| ❌ `Could not resolve placeholder '...'` | Une variable manque dans `backend/.env.properties` (ou le back est lancé hors du dossier `backend`). |
| 🔒 Swagger UI répond 401 | L'accès à la documentation de l'API nécessite pour l'instant un jeton JWT. |
| 🟢 Erreur de version de Node au `npm install` | Utilise Node 22.22+, 24.15+ ou 26+ (voir Prérequis). |
| 📧 La création d'un compte échoue (délai de 5 s puis erreur) | L'e-mail d'activation est indispensable : vérifie les variables `MAIL_*` et le mot de passe d'application Gmail. En développement, utilise les comptes de démo déjà actifs. |
| 📭 Les e-mails de notification n'arrivent pas | Ils sont envoyés en tâche de fond : une panne SMTP est seulement journalisée dans les logs du back, sans bloquer l'action. |

## 👥 Équipe

Projet réalisé par l'équipe **CDA-team1** : Anthony Gargoua, Damien Castello et Garance Guinet. 💪
