import { TestBed } from '@angular/core/testing';

import { CommentDto } from '../../../../domain/event.model';
import { CommentList } from './comment-list';

describe('CommentList', () => {
  const comments: CommentDto[] = [
    {
      id: 1,
      eventId: 10,
      authorDisplayName: 'John D.',
      content: 'Super initiative !',
      createdAt: '2026-09-10T14:00:00',
    },
    {
      id: 2,
      eventId: 10,
      authorDisplayName: 'Elliot A.',
      content: "N'hésitez pas si besoin de bénévoles.",
      createdAt: '2026-09-11T09:30:00',
    },
  ];

  async function render(value: CommentDto[]) {
    const fixture = TestBed.createComponent(CommentList);
    fixture.componentRef.setInput('comments', value);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it("affiche l'auteur, la date et le texte de chaque commentaire", async () => {
    const element = await render(comments);

    const first = element.querySelector('.comment')!;
    expect(first.querySelector('.comment__author')?.textContent).toBe('John D.');
    expect(first.querySelector('.comment__date')?.textContent?.trim()).toBe('10/09/2026 14:00');
    expect(first.querySelector('.comment__content')?.textContent).toBe('Super initiative !');
  });

  it('affiche du HTML saisi dans un commentaire comme du texte, sans jamais l’interpréter', async () => {
    const payload =
      '<img src="x" onerror="window.__xss = true"><script>window.__xss = true</script><b>gras</b>';
    const element = await render([
      { ...comments[0], authorDisplayName: '<b>Pirate</b>', content: payload },
    ]);

    const content = element.querySelector('.comment__content')!;
    expect(content.textContent).toBe(payload);
    expect(content.children).toHaveLength(0);
    expect(element.querySelector('.comment__author')?.children).toHaveLength(0);
    expect(element.querySelector('img, script, b')).toBeNull();
    expect((window as unknown as Record<string, unknown>)['__xss']).toBeUndefined();
  });

  it("garde l'ordre reçu", async () => {
    const element = await render(comments);

    const authors = Array.from(element.querySelectorAll('.comment__author')).map(
      (a) => a.textContent,
    );
    expect(authors).toEqual(['John D.', 'Elliot A.']);
  });

  it("affiche un message quand il n'y a aucun commentaire", async () => {
    const element = await render([]);

    expect(element.querySelector('.comment')).toBeNull();
    expect(element.textContent).toContain('Aucun commentaire pour le moment.');
  });
});
