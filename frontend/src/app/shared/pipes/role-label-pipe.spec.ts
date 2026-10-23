import { RoleLabelPipe } from './role-label-pipe';

describe('RoleLabelPipe', () => {
  it.each([
    ['MEMBER', 'Membre'],
    ['ORGANIZER', 'Organisateur'],
    ['ADMIN', 'Administrateur'],
  ] as const)('%s → %s', (role, expected) => {
    expect(new RoleLabelPipe().transform(role)).toBe(expected);
  });
});
