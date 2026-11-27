export interface AuditEntry {
  id: number;
  appointment_id: number;
  actor_id: number | null;
  actor_name: string | null;
  action: string;
  before_json: string;
  after_json: string;
  reason: string;
  created_at: string;
}
