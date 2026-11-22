import type { Appointment } from '../appointment/Appointment';

export interface AppointmentTableProps {
  visible: Appointment[];
  staff: boolean;
  busy: boolean;
  onAction: (row: Appointment, name: string) => Promise<void>;
}
