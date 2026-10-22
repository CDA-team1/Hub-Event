# 🎨 Hub-Event : front-end Angular

Interface web de la plateforme Hub-Event (voir le [README du dépôt](../README.md) pour la présentation du projet et l'installation complète).

**Stack** : Angular 22 · composants autonomes · signaux · Vitest · Prettier.

## 🚀 Démarrage

Prérequis : Node.js `^22.22.3`, `^24.15.0` ou `>=26`, et npm 11. Le back doit tourner sur `http://localhost:8080` (voir le README du dépôt).

```bash
npm install
npm start
```

L'application est servie sur **http://localhost:4200**. Le serveur de développement redirige tous les appels `/api/*` vers le back grâce à `proxy.conf.json`. L'adresse de l'API (`/api`) est définie dans `src/environments/` et injectée par le jeton `API_URL`.

## 🧪 Commandes utiles

| Commande | Rôle |
|---|---|
| `npm start` | Serveur de développement avec rechargement automatique |
| `npm test` | Tests Vitest en mode surveillance |
| `npx ng test --watch=false` | Tests, une seule exécution |
| `npm run build` | Build de production dans `dist/` |
| `npm run lint` | Vérifie le code avec ESLint |
| `npx prettier --check "src/**/*.{ts,html,css}"` | Vérifie le formatage |

## 📁 Structure de `src/app`

```text
src/app/
├── core/        accès à l'API, authentification, intercepteurs HTTP, stockage de session
├── domain/      modèles et règles métier pures (sans Angular), avec leurs tests
├── features/    une page = un dossier : account, auth, calendar, clubs, events,
│                not-found, privacy
└── shared/ui/   composants réutilisables : tableau, carte, badge, pagination,
                 boîte de confirmation, états vide / erreur / chargement…
```

Règles d'organisation :

- **Un composant utilisé par une seule page vit dans le dossier de cette page** (ex. `event-detail-page/image-gallery/`). Il ne passe dans `shared/ui` que s'il sert à plusieurs pages.
- **Les pages décident**, les composants d'affichage reçoivent des entrées et n'émettent que des évènements. Ce sont les pages qui appellent les services.
- **Les services** (`core/`) portent les appels HTTP, un par domaine (`event-api`, `account-api`, `club-api`…). Les données sont exposées en signaux.
- **L'authentification** : `core/auth` (jeton JWT conservé dans le `sessionStorage`), `authGuard` et `roleGuard('ADMIN' | 'ORGANIZER'…)` sur les routes sensibles, et deux intercepteurs : `jwt-interceptor` ajoute le jeton, `error-interceptor` renvoie vers la connexion quand la session expire (401).

Les notes de conception (arbre des composants, un fichier par domaine) sont dans [`../conception/frontend/`](../conception/frontend/).

## 🧱 Composants partagés à connaître

- **`DataTable`** : tableau générique dont l'appelant définit les cellules avec `appColumn`. Options : colonne `sortable` (le tri reste à la charge de la page), `hideOnSmall` (masque la colonne en dessous de 640 px) et l'entrée `bordered` (cadre arrondi, en-tête grisé, traits entre les cellules).
- **`EventCard`** : carte d'un évènement. Sans photo, elle affiche l'illustration de sa catégorie (`domain/default-event-image.ts`, fichiers dans `public/placeholders/`).
- **`ActionButton`** : bouton d'action à icône, avec libellé accessible et niveau de couleur.

## ✍️ Conventions

- **Formatage** : Prettier (largeur 100, guillemets simples, indentation de 2 espaces), configuré dans `.prettierrc` et `.editorconfig`.
- **Style** : CSS par composant, point de rupture mobile à **640 px**. Le fond de l'application est un beige uniforme défini dans `src/styles.css`.
- **Tests** : un fichier `*.spec.ts` à côté de chaque composant, service ou règle de domaine. Les noms de tests sont écrits en français et décrivent le comportement (« affiche… », « refuse… »). Les appels HTTP sont simulés avec `HttpTestingController`.
- **Accessibilité** : libellés `aria-label` sur les boutons à icône, rôles sur les messages d'erreur (`role="alert"`).

## 🔒 Sécurité : XSS et CSP

- **Pas d'injection HTML** : tout texte saisi ou reçu du back (commentaires, titre, description…) est affiché par interpolation `{{ }}`, qu'Angular échappe. Le code n'utilise ni `[innerHTML]` ni `bypassSecurityTrust…` : ne pas en introduire pour des données saisies. Des tests (`comment-list`, `event-detail-page`) vérifient qu'un `<img onerror=…>` ou un `<script>` injecté reste du texte.
- **Content Security Policy** : la balise est dans `src/index.prod.html`, utilisée uniquement par la build de production (`angular.json`, option `index`). Elle est absente en développement, où elle bloquerait le rechargement automatique. À mettre à jour si on ajoute un hébergeur d'images (`img-src`) ou si le back est servi depuis un autre domaine (`connect-src`). Le reste de `index.prod.html` doit rester identique à `index.html`.
- **En-têtes HTTP à demander à l'hébergeur** : une balise `<meta>` ne peut pas porter `frame-ancestors` (anti-clickjacking). Envoyer `Content-Security-Policy: frame-ancestors 'none'` et `X-Content-Type-Options: nosniff` avec les pages du front.
- **Vérifier après un `npm run build`** : `dist/hub-event/browser/index.html` contient la balise CSP et aucun attribut `onload`, et l'application ne produit aucune erreur de CSP dans la console.

## 🔧 Dépannage

| Symptôme | Cause et solution |
|---|---|
| Erreur de version de Node au `npm install` | Utilise Node 22.22+, 24.15+ ou 26+. |
| Les appels `/api/...` échouent (404 ou erreur réseau) | Le back n'est pas lancé sur le port 8080, ou le front n'a pas été démarré avec `npm start` (c'est lui qui applique le proxy). |
| Une image de placeholder ne s'affiche pas | Après avoir ajouté un fichier dans `public/`, relance `npm start` : le serveur ne détecte pas toujours les nouveaux dossiers. |
