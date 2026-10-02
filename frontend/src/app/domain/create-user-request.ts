export interface CreateUserRequest {
  lastName: string;
  firstName: string;
  postalAddress: string;
  email: string;
  phone: string | null;
  password: string;
}
