import { AppointmentList } from '../../appointments/components/AppointmentList';

export function StaffAppointments() {
  return (
    <>
      <h1>Quản lý lịch hẹn</h1>
      <AppointmentList staff />
    </>
  );
}
