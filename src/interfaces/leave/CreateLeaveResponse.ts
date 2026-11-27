import type { AffectedAppointment } from './AffectedAppointment';

export interface CreateLeaveResponse {
  id: number;
  affected: AffectedAppointment[];
  message: string;
}
