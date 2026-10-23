/** Suspension d'un compte par l'admin : `endDate` à null = suspension définitive (CU30). */
export interface SuspendUserRequest {
  reason: string;
  endDate: string | null;
}
