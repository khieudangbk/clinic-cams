export type Role = 'patient' | 'receptionist' | 'doctor' | 'manager';

export interface User {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  role: Role;
  active: 0 | 1;
  specialty_id: number | null;
}
