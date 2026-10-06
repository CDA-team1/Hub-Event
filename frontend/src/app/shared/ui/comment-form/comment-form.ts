import { Component, output, signal } from '@angular/core';
import { FormField, form, required, submit } from '@angular/forms/signals';

@Component({
  selector: 'app-comment-form',
  imports: [FormField],
  templateUrl: './comment-form.html',
  styleUrl: './comment-form.css',
})
export class CommentForm {
  readonly submitted = output<string>();

  protected readonly model = signal({
    content: '',
  });

  protected readonly commentForm = form(this.model, (path) => {
    required(path.content, {
      message: 'Veuillez saisir un commentaire.',
    });
  });

  protected readonly blankError = signal(false);

  protected async onSubmit(event: Event): Promise<void> {
    event.preventDefault();

    await submit(this.commentForm, async (field) => {
      const content = field().value().content.trim();

      if (!content) {
        this.blankError.set(true);
        return undefined;
      }

      this.blankError.set(false);
      this.submitted.emit(content);

      return undefined;
    });
  }

  protected cancel(): void {
    this.reset();
  }

  reset(): void {
    this.model.set({
      content: '',
    });
    this.blankError.set(false);
  }
}
