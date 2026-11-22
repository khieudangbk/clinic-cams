export type Status = 'pending' | 'confirmed' | 'arrived' | 'completed' | 'cancelled' | 'noshow';

export interface Appointment {
  id: number;
  patient_id: number | null;
  slot_id: number;
  doctor_id: number | null;
  starts_at: string | null;
  ends_at: string | null;
  doctor_name: string | null;
  patient_name: string | null;
  patient_phone: string | null;
  status: Status;
  channel: 'online' | 'staff';
  reason: string;
  reschedule_count: number;
  created_by: number | null;
  created_at: string;
  doctor_on_leave: 0 | 1;
  booking_details: string;
}
