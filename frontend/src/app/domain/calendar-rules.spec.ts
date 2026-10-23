import {
  addDays,
  addMonths,
  buildCalendarWeeks,
  daysInRange,
  endOfMonth,
  endOfWeek,
  shiftRange,
  startOfMonth,
  startOfWeek,
  toIsoDate,
} from './calendar-rules';

describe('toIsoDate', () => {
  it('formate une date en yyyy-MM-dd', () => {
    expect(toIsoDate(new Date(2026, 9, 6))).toBe('2026-10-06');
  });
});

describe('addDays', () => {
  it('avance ou recule de n jours, y compris à travers un changement de mois', () => {
    expect(toIsoDate(addDays(new Date(2026, 9, 30), 3))).toBe('2026-11-02');
    expect(toIsoDate(addDays(new Date(2026, 9, 1), -1))).toBe('2026-09-30');
  });
});

describe('addMonths', () => {
  it('avance ou recule de n mois', () => {
    expect(toIsoDate(addMonths(new Date(2026, 0, 15), 1))).toBe('2026-02-15');
    expect(toIsoDate(addMonths(new Date(2026, 0, 15), -1))).toBe('2025-12-15');
  });
});

describe('startOfWeek / endOfWeek', () => {
  it.each([
    [new Date(2026, 9, 6), '2026-10-05', '2026-10-11'], // mardi
    [new Date(2026, 9, 5), '2026-10-05', '2026-10-11'], // lundi lui-même
    [new Date(2026, 9, 11), '2026-10-05', '2026-10-11'], // dimanche
  ])('%s → semaine %s - %s', (date, expectedStart, expectedEnd) => {
    expect(toIsoDate(startOfWeek(date))).toBe(expectedStart);
    expect(toIsoDate(endOfWeek(date))).toBe(expectedEnd);
  });
});

describe('startOfMonth / endOfMonth', () => {
  it('renvoie le premier et le dernier jour du mois', () => {
    const date = new Date(2026, 1, 14); // février, année non bissextile
    expect(toIsoDate(startOfMonth(date))).toBe('2026-02-01');
    expect(toIsoDate(endOfMonth(date))).toBe('2026-02-28');
  });
});

describe('daysInRange', () => {
  it('compte les jours des deux bornes incluses', () => {
    expect(daysInRange('2026-10-05', '2026-10-11')).toBe(7);
    expect(daysInRange('2026-10-05', '2026-10-05')).toBe(1);
  });
});

describe('shiftRange', () => {
  it('décale une période en conservant sa longueur', () => {
    expect(shiftRange('2026-10-05', '2026-10-11', 7)).toEqual({
      from: '2026-10-12',
      to: '2026-10-18',
    });
    expect(shiftRange('2026-10-05', '2026-10-11', -7)).toEqual({
      from: '2026-09-28',
      to: '2026-10-04',
    });
  });
});

describe('buildCalendarWeeks', () => {
  it('ne crée qu’une semaine quand la période tient dans une semaine complète', () => {
    const weeks = buildCalendarWeeks('2026-10-05', '2026-10-11');
    expect(weeks).toHaveLength(1);
    expect(weeks[0].map((day) => day.date)).toEqual([
      '2026-10-05',
      '2026-10-06',
      '2026-10-07',
      '2026-10-08',
      '2026-10-09',
      '2026-10-10',
      '2026-10-11',
    ]);
    expect(weeks[0].every((day) => day.inRange)).toBe(true);
  });

  it('complète avec des jours de bourrage marqués inRange: false', () => {
    // octobre 2026 : le 1er est un jeudi, le 31 un samedi
    const weeks = buildCalendarWeeks('2026-10-01', '2026-10-31');
    const firstWeek = weeks[0];
    const lastWeek = weeks[weeks.length - 1];

    expect(firstWeek[0].date).toBe('2026-09-28');
    expect(firstWeek[0].inRange).toBe(false);
    expect(firstWeek[3].date).toBe('2026-10-01');
    expect(firstWeek[3].inRange).toBe(true);

    expect(lastWeek[5].date).toBe('2026-10-31');
    expect(lastWeek[5].inRange).toBe(true);
    expect(lastWeek[6].inRange).toBe(false);
  });
});
