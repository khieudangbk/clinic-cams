export interface SaveScheduleRequest {
  doctor_id: number;
  weekdays: number[];
  start_time: string;
  end_time: string;
}
