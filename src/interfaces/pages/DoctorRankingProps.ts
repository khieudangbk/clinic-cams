import type { Appointment } from '../appointment/Appointment';

export interface DoctorRankingProps {
  filtered: Appointment[];
  doctorNames: [number | null, string | null][];
}
