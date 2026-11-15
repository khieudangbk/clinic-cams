import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { BarChart3, CalendarDays, Users, Clock, LogOut, PlusCircle, ArrowLeft } from 'lucide-react';
import { useApp } from '../context';
import { roles } from '../types';
import { Logo } from '../components/Layout';
import { AppointmentList } from './Appointments';
export function StaffLayout() {
  const { user, logout, notify } = useApp();
  const location = useLocation();
  if (!user)
    return (
      <div className="container page auth-card">
        <Logo />
        <h1>Khu vực nhân viên</h1>
        <p>Đăng nhập tài khoản lễ tân, bác sĩ hoặc quản lý.</p>
        <Link className="primary" to={'/dang-nhap?next=' + encodeURIComponent(location.pathname)}>
          Đăng nhập nhân viên
        </Link>
        <Link to="/">← Trang chủ</Link>
      </div>
    );
  if (user.role === 'patient')
    return (
      <div className="container page auth-card">
        <h1>Khu vực dành cho nhân viên</h1>
        <p>Tài khoản của bạn dùng để đặt và theo dõi lịch khám cá nhân.</p>
        <Link to="/lich-hen" className="primary">
          Lịch hẹn của tôi
        </Link>
      </div>
    );
  const nav = [
    ['/nhan-vien', 'Tổng quan', BarChart3],
    ['/nhan-vien/lich-hen', 'Lịch hẹn', CalendarDays],
    ...(user.role === 'receptionist' ? [['/nhan-vien/dat-lich', 'Đặt lịch hộ', PlusCircle]] : []),
    ...(['doctor', 'manager'].includes(user.role)
      ? [['/nhan-vien/lich-bac-si', 'Lịch bác sĩ', Clock]]
      : []),
    ...(user.role === 'manager' ? [['/nhan-vien/quan-tri', 'Quản trị', Users]] : []),
  ] as [string, string, typeof BarChart3][];
  return (
    <div className="staff-shell">
      <aside className="staff-sidebar">
        <Logo />
        <p>QUẢN LÝ PHÒNG KHÁM</p>
        <nav>
          {nav.map(([to, label, Icon]) => (
            <NavLink to={to} key={to as string} end={to === '/nhan-vien'}>
              <Icon size={18} />
              {label as string}
            </NavLink>
          ))}
        </nav>
        <div className="staff-user">
          <strong>{user.name}</strong>
          <small>{roles[user.role]}</small>
          <Link to="/">
            <ArrowLeft size={16} /> Trang chủ đặt khám
          </Link>
          <button
            onClick={async () => {
              try {
                await logout();
              } catch (e) {
                notify((e as Error).message, true);
              }
            }}
          >
            <LogOut size={16} />
            Đăng xuất
          </button>
        </div>
      </aside>
      <main className="staff-main">
        <header>
          <p>PHÒNG KHÁM CAMS</p>
          <span>Không gian làm việc của {roles[user.role].toLowerCase()}</span>
        </header>
        <Outlet />
      </main>
    </div>
  );
}
export function StaffAppointments() {
  return (
    <>
      <h1>Quản lý lịch hẹn</h1>
      <AppointmentList staff />
    </>
  );
}
