import type { AuditEntry } from '../appointment/AuditEntry';

export interface AuditHistoryModalProps {
  audit: AuditEntry[];
  onClose: () => void;
}
