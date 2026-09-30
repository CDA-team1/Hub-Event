import { Component, inject, signal } from '@angular/core';
import { email, FormField, form, required, submit } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { Auth, loginErrorMessage } from '../../../core/auth/auth';

@Component({
  selector: 'app-login-page',
  imports: [FormField, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly credentials = signal({ email: '', password: '' });
  protected readonly loginForm = form(this.credentials, (path) => {
    required(path.email, { message: "L'email est obligatoire." });
    email(path.email, { message: 'Cet email n’est pas valide.' });
    required(path.password, { message: 'Le mot de passe est obligatoire.' });
  });
  protected readonly serverError = signal('');

  protected async login(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.loginForm, async (field) => {
      const { email, password } = field().value();
      this.serverError.set('');
      try {
        await this.auth.login(email.trim(), password);
        await this.router.navigate(['/']);
      } catch (error) {
        this.serverError.set(loginErrorMessage(error));
      }
      return undefined;
    });
  }
}
