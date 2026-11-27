import type { AppointmentAction } from '../appointment/AppointmentAction';

export interface AppointmentActionModalProps {
  action: AppointmentAction;
  busy: boolean;
  onBusyChange: (value: boolean) => void;
  onClose: () => void;
  onUpdated: () => Promise<void>;
}
