import type { AdminRequests } from './AdminRequests';

export type AdminSave = <P extends keyof AdminRequests>(
  path: P,
  data: AdminRequests[P],
) => Promise<boolean>;
