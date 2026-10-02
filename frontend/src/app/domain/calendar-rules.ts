export interface CalendarDay {
  /** Date au format ISO (yyyy-MM-dd). */
  readonly date: string;
  readonly dayOfMonth: number;
  /** Faux pour les jours de bourrage, hors période demandée, ajoutés pour compléter la grille. */
  readonly inRange: boolean;
}

export type CalendarWeek = readonly CalendarDay[];

function parseIsoDate(iso: string): Date {
  const [year, month, day] = iso.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

export function addMonths(date: Date, months: number): Date {
  const result = new Date(date);
  result.setMonth(result.getMonth() + months);
  return result;
}

/** Lundi de la semaine contenant `date`. */
export function startOfWeek(date: Date): Date {
  const mondayOffset = (date.getDay() + 6) % 7;
  return addDays(date, -mondayOffset);
}

/** Dimanche de la semaine contenant `date`. */
export function endOfWeek(date: Date): Date {
  return addDays(startOfWeek(date), 6);
}

export function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

export function endOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}

/** Nombre de jours entre deux dates ISO incluses (ex. du lundi au dimanche même semaine → 7). */
export function daysInRange(from: string, to: string): number {
  const ms = parseIsoDate(to).getTime() - parseIsoDate(from).getTime();
  return Math.round(ms / 86_400_000) + 1;
}

/** Décale une période [from, to] de `days` jours, en conservant sa longueur. */
export function shiftRange(from: string, to: string, days: number): { from: string; to: string } {
  return {
    from: toIsoDate(addDays(parseIsoDate(from), days)),
    to: toIsoDate(addDays(parseIsoDate(to), days)),
  };
}

/**
 * Découpe une période [from, to] en semaines complètes (lundi → dimanche). Les jours hors
 * période sont inclus pour compléter la grille, avec `inRange: false`.
 */
export function buildCalendarWeeks(from: string, to: string): CalendarWeek[] {
  const fromDate = parseIsoDate(from);
  const toDate = parseIsoDate(to);
  const gridStart = startOfWeek(fromDate);
  const gridEnd = endOfWeek(toDate);

  const weeks: CalendarWeek[] = [];
  for (let weekStart = gridStart; weekStart <= gridEnd; weekStart = addDays(weekStart, 7)) {
    const week: CalendarDay[] = [];
    for (let i = 0; i < 7; i++) {
      const day = addDays(weekStart, i);
      week.push({
        date: toIsoDate(day),
        dayOfMonth: day.getDate(),
        inRange: day >= fromDate && day <= toDate,
      });
    }
    weeks.push(week);
  }
  return weeks;
}
