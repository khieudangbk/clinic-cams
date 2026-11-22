import type { BookingDetails } from './BookingDetails';

export interface CreateAppointmentRequest {
  slot_id: number;
  reason: string;
  patient_name?: string;
  patient_phone?: string;
  patient_email?: string;
  booking_details?: BookingDetails | null;
}
