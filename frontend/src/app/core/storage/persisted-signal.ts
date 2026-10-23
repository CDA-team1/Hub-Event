import { WritableSignal, effect, signal } from '@angular/core';

/**
 * Signal dont la valeur est sauvegardée dans `storage` à chaque modification (`sessionStorage`
 * ou `localStorage` selon la durée de vie voulue). `isValid` vérifie la valeur relue : le
 * contenu du storage n'est pas fiable. À appeler dans un contexte d'injection.
 */
export function persistedSignal<T>(
  key: string,
  initial: T,
  isValid: (value: unknown) => value is T,
  storage: Storage,
): WritableSignal<T> {
  const state = signal<T>(read(key, initial, isValid, storage));
  effect(() => {
    const value = state();
    try {
      if (value === null || value === undefined) {
        storage.removeItem(key);
      } else {
        storage.setItem(key, JSON.stringify(value));
      }
    } catch {
      // quota dépassé ou stockage indisponible : l'application reste utilisable
    }
  });
  return state;
}

function read<T>(
  key: string,
  initial: T,
  isValid: (value: unknown) => value is T,
  storage: Storage,
): T {
  try {
    const raw = storage.getItem(key);
    if (raw !== null) {
      const parsed: unknown = JSON.parse(raw);
      if (isValid(parsed)) {
        return parsed;
      }
    }
  } catch {
    // JSON corrompu ou stockage indisponible
  }
  return initial;
}
