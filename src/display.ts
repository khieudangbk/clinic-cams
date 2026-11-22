import type { Role } from './interfaces/account/User';
import type { Status } from './interfaces/appointment/Appointment';

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
export const dateText = (s: string | null) =>
  s
    ? new Date(s).toLocaleString('vi-VN', {
        timeZone: 'Asia/Ho_Chi_Minh',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    : '—';
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
