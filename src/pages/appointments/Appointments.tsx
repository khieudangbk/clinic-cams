import { Link } from 'react-router-dom';
import { useApp } from '../../context';
import { Breadcrumb } from '../../components/UI';
import { AppointmentList } from './components/AppointmentList';

export function Appointments() {
  const { user } = useApp();
  if (!user)
    return (
      <div className="container page auth-card">
        <h1>Lịch hẹn của tôi</h1>
        <p>Đăng nhập để xem và quản lý lịch khám của bạn.</p>
        <Link className="primary" to="/dang-nhap?next=/lich-hen">
          Đăng nhập
        </Link>
      </div>
    );
  if (user.role !== 'patient')
    return (
      <div className="container page">
        <h1>Lịch hẹn phòng khám</h1>
        <Link to="/nhan-vien/lich-hen" className="primary">
          Mở danh sách trong khu vực nhân viên
        </Link>
      </div>
    );
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Lịch hẹn của tôi' }]} />
      <div className="section-heading">
        <h1>Lịch hẹn của tôi</h1>
        <Link to="/bac-si" className="see-more">
          Đặt lịch mới
        </Link>
      </div>
      <AppointmentList />
    </div>
  );
}
