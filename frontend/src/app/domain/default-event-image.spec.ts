import { Category } from './category';
import { defaultEventImage } from './default-event-image';

describe('defaultEventImage', () => {
  it('donne une illustration différente pour chaque catégorie', () => {
    const categories: Category[] = ['CULTURE', 'LEISURE', 'SPORT'];

    const images = categories.map((category) => defaultEventImage(category));

    expect(new Set(images).size).toBe(3);
    expect(defaultEventImage('SPORT')).toBe('placeholders/sport.webp');
  });
});
