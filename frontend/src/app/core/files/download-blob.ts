/** Déclenche le téléchargement d'un fichier déjà en mémoire (PDF, Excel...) sous le nom donné. */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}
