import type { User } from '../account/User';
import type { AdminSave } from '../admin/AdminSave';

export interface StaffAccountsProps {
  users: User[];
  save: AdminSave;
}
