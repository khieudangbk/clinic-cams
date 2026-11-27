import type { Schedule } from '../schedule/Schedule';

export interface WeeklyScheduleProps {
  current: Schedule | undefined;
  manager: boolean;
  doctor: string;
  onSaved: () => void;
}
