import type { Status } from '../appointment/Appointment';

export interface AffectedAppointment {
  id: number;
  status: Status;
  starts_at: string | null;
  patient_name: string | null;
  patient_phone: string | null;
}
