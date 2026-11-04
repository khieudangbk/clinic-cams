export type Role = 'patient' | 'receptionist' | 'doctor' | 'manager';
export type User = {
  id: number;
  name: string;
  email: string;
  phone: string;
  role: Role;
  active: number;
  specialty_id?: number;
};
export type Specialty = { id: number; name: string };
export type Doctor = { id: number; name: string; specialty_id: number; specialty_name: string };
export type Slot = { id: number; starts_at: string; ends_at: string };
export type Schedule = {
  doctor_id: number;
  weekdays: string;
  start_time: string;
  end_time: string;
};
export type Catalog = {
  specialties: Specialty[];
  doctors: Doctor[];
  now: string;
  clock_mode: string;
  schedules?: Schedule[];
};
export type Status = 'pending' | 'confirmed' | 'arrived' | 'completed' | 'cancelled' | 'noshow';
export type Appointment = Slot & {
  patient_id: number;
  doctor_id: number;
  doctor_name: string;
  patient_name: string;
  patient_phone: string;
  status: Status;
  channel: string;
  reason: string;
  reschedule_count: number;
  doctor_on_leave: number;
  booking_details?: string;
};
export const statuses: Record<Status, string> = {
  pending: 'Chờ xác nhận',
  confirmed: 'Đã xác nhận',
  arrived: 'Đã đến',
  completed: 'Hoàn thành',
  cancelled: 'Đã hủy',
  noshow: 'Vắng mặt',
};
export const roles: Record<Role, string> = {
  patient: 'Bệnh nhân',
  receptionist: 'Lễ tân',
  doctor: 'Bác sĩ',
  manager: 'Quản lý',
};
export const dateText = (s: string) =>
  new Date(s).toLocaleString('vi-VN', {
    timeZone: 'Asia/Ho_Chi_Minh',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
export const dayOffset = (s: string, n: number) =>
  new Date(
    new Date(s.slice(0, 10) + 'T12:00:00+07:00').getTime() + n * 86400000,
  ).toLocaleDateString('en-CA', { timeZone: 'Asia/Ho_Chi_Minh' });
export const normalize = (s: string) =>
  s
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/gi, 'd')
    .toLowerCase();
