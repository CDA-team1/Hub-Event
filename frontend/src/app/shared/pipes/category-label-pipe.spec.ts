import { CategoryLabelPipe } from './category-label-pipe';

describe('CategoryLabelPipe', () => {
  it.each([
    ['CULTURE', 'Culture'],
    ['SPORT', 'Sport'],
    ['LEISURE', 'Loisirs'],
  ] as const)('%s → %s', (category, expected) => {
    expect(new CategoryLabelPipe().transform(category)).toBe(expected);
  });
});
