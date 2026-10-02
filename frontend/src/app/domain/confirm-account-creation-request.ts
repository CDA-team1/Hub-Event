export interface ConfirmAccountCreationRequest {
  temporaryPassword: string;
  newPassword: string;
  confirmPassword: string;
}
