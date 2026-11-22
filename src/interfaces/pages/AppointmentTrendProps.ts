import type { Appointment } from '../appointment/Appointment';

export interface AppointmentTrendProps {
  filtered: Appointment[];
  days: string[];
  range: string;
}
