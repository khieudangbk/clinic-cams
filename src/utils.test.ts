import { describe, it, expect } from 'vitest';
import { summarizeAppointments, csvCell } from './utils';
import { normalize, dayOffset } from './display';
import type { Appointment } from './interfaces/appointment/Appointment';
const a = (id: number, day: string, status = 'pending', doctor = 4) =>
  ({ id, doctor_id: doctor, starts_at: day + 'T10:00:00+07:00', status }) as Appointment;
const rows = [
  a(1, '2026-11-23', 'pending'),
  a(2, '2026-11-17', 'completed'),
  a(3, '2026-11-16', 'cancelled', 5),
  a(4, '2026-11-24', 'confirmed'),
  a(5, '2026-10-01', 'completed', 5),
  a(6, '2026-11-23', 'completed'),
];
describe('Activity dashboard aggregation', () => {
  it('includes both boundaries of a seven-day period, excludes future slots', () =>
    expect(summarizeAppointments(rows, '2026-11-23', '7').filtered.map((x) => x.id)).toEqual([
      1, 2, 6,
    ]));
  it('includes a 30-day period', () =>
    expect(summarizeAppointments(rows, '2026-11-23', '30').filtered.map((x) => x.id)).toEqual([
      1, 2, 3, 6,
    ]));
  it('all time still excludes future appointments', () =>
    expect(summarizeAppointments(rows, '2026-11-23', 'all').filtered.map((x) => x.id)).toEqual([
      1, 2, 3, 5, 6,
    ]));
  it('filters a doctor before computing counts', () =>
    expect(summarizeAppointments(rows, '2026-11-23', 'all', '5').filtered.map((x) => x.id)).toEqual(
      [3, 5],
    ));
  it('queue includes only active appointments today', () =>
    expect(summarizeAppointments(rows, '2026-11-23', '7').queue.map((x) => x.id)).toEqual([1]));
  it('does not invent records outside the role-scoped API input', () =>
    expect(summarizeAppointments([rows[1]], '2026-11-23', 'all').filtered).toHaveLength(1));
  it('ignores appointments whose joined slot is missing', () => {
    const missingSlot: Appointment = {
      ...rows[0],
      starts_at: null,
      ends_at: null,
      doctor_id: null,
    };
    const result = summarizeAppointments([missingSlot, rows[0]], '2026-11-23', '7');
    expect(result.filtered.map((row) => row.id)).toEqual([1]);
    expect(result.queue.map((row) => row.id)).toEqual([1]);
    expect(result.scoped).toHaveLength(2);
  });
  it('handles empty input', () =>
    expect(summarizeAppointments([], '2026-11-23', 'all')).toMatchObject({
      filtered: [],
      queue: [],
    }));
});
describe('Vietnamese search and dates', () => {
  it('matches accents and đ', () =>
    expect(normalize('ĐẶT KHÁM Tim mạch')).toBe('dat kham tim mach'));
  it('handles month and year boundaries in Vietnam', () => {
    expect(dayOffset('2026-01-01', -1)).toBe('2025-12-31');
    expect(dayOffset('2026-11-23', 30)).toBe('2026-12-23');
  });
});
describe('CSV export', () => {
  it('quotes commas and double quotes', () => expect(csvCell('A,"B"')).toBe('"A,""B"""'));
  it('neutralizes spreadsheet formula prefixes', () => {
    for (const s of ['=SUM(A1)', '+1', '-1', '@CMD', '\tformula', '\rformula'])
      expect(csvCell(s)).toBe('"\'' + s + '"');
  });
  it('exports zero and blanks correctly', () => {
    expect(csvCell(0)).toBe('"0"');
    expect(csvCell(null)).toBe('""');
  });
});
