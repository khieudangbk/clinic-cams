export interface CreateStaffUserRequest {
  role: 'doctor' | 'receptionist';
  name: string;
  email: string;
  password: string;
  specialty_id?: number | null;
}
