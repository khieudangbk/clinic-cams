import { dayOffset } from './display';
import type { Appointment } from './interfaces/appointment/Appointment';
export function summarizeAppointments(
  rows: Appointment[],
  today: string,
  range: string,
  doctor = '',
) {
  const start = range === 'all' ? '0000' : dayOffset(today, 1 - Number(range));
  const scoped = rows.filter((x) => !doctor || String(x.doctor_id) === doctor);
  const filtered = scoped.filter(
    (x) =>
      x.starts_at != null && x.starts_at.slice(0, 10) >= start && x.starts_at.slice(0, 10) <= today,
  );
  const queue = scoped
    .filter(
      (x) =>
        x.starts_at?.startsWith(today) && ['pending', 'confirmed', 'arrived'].includes(x.status),
    )
    .sort((a, b) => (a.starts_at ?? '').localeCompare(b.starts_at ?? ''));
  return { filtered, queue, start, scoped };
}
export const csvCell = (v: unknown) =>
  '"' +
  String(v ?? '')
    .replace(/^[=+@\-\t\r]/, "'$&")
    .replaceAll('"', '""') +
  '"';
