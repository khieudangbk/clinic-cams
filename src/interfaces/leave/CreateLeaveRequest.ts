export interface CreateLeaveRequest {
  doctor_id?: number;
  starts_at: string;
  ends_at: string;
  reason: string;
}
