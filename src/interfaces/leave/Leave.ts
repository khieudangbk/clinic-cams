export interface Leave {
  id: number;
  doctor_id: number;
  doctor_name: string | null;
  starts_at: string;
  ends_at: string;
  reason: string;
  created_by: number | null;
}
