import { HttpContextToken } from '@angular/common/http';

/**
 * Étiquette qu'on colle sur une requête pour dire à l'intercepteur d'erreurs : "si ça répond
 * 401, ce n'est pas une session expirée, ne redirige pas et n'efface pas la session". Utile
 * pour le login lui-même, où un 401 veut juste dire "mauvais email ou mot de passe".
 */
export const SKIP_AUTH_REDIRECT = new HttpContextToken<boolean>(() => false);
