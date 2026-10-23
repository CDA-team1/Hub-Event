import { EventSearchCriteria } from './event.model';
import { criteriaFromParams, criteriaToParams } from './event-search';

describe('critères de recherche', () => {
  it("lit tous les critères depuis l'URL", () => {
    const params = new URLSearchParams(
      'category=SPORT&minPrice=5&maxPrice=20&location=Lyon&startDate=2026-10-01&endDate=2026-10-31&keywords=yoga',
    );

    expect(criteriaFromParams(params)).toEqual({
      category: 'SPORT',
      minPrice: 5,
      maxPrice: 20,
      location: 'Lyon',
      startDate: '2026-10-01',
      endDate: '2026-10-31',
      keywords: 'yoga',
    });
  });

  it("ne retient aucun critère quand l'URL est vide", () => {
    expect(criteriaFromParams(new URLSearchParams())).toEqual({});
  });

  it('ignore les valeurs invalides', () => {
    const params = new URLSearchParams(
      'category=DANSE&minPrice=abc&maxPrice=-3&location=%20%20&startDate=16/10/2026&endDate=demain&keywords=',
    );

    expect(criteriaFromParams(params)).toEqual({});
  });

  it('supprime les espaces autour du texte', () => {
    const params = new URLSearchParams('location=%20Lyon%20&keywords=%20jeux%20');

    expect(criteriaFromParams(params)).toEqual({ location: 'Lyon', keywords: 'jeux' });
  });

  it('accepte un prix à zéro (événement gratuit)', () => {
    expect(criteriaFromParams(new URLSearchParams('maxPrice=0'))).toEqual({ maxPrice: 0 });
  });

  it('ne produit que les paramètres renseignés', () => {
    const criteria: EventSearchCriteria = { category: 'SPORT', minPrice: 0, keywords: 'yoga' };

    expect(criteriaToParams(criteria)).toEqual({
      category: 'SPORT',
      minPrice: '0',
      keywords: 'yoga',
    });
  });

  it("retrouve les mêmes critères après un aller-retour par l'URL", () => {
    const criteria: EventSearchCriteria = {
      category: 'LEISURE',
      minPrice: 2,
      maxPrice: 10,
      location: 'Toulouse',
      startDate: '2026-11-01',
      endDate: '2026-11-30',
      keywords: 'jeux',
    };

    const url = new URLSearchParams(criteriaToParams(criteria));

    expect(criteriaFromParams(url)).toEqual(criteria);
  });
});
