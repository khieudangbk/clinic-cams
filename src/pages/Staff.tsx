import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { BarChart3, CalendarDays, Users, Clock, LogOut, PlusCircle, ArrowLeft } from 'lucide-react';
import { api } from '../api';
import { useApp } from '../context';
import { roles, type Slot } from '../types';
import { Logo } from '../components/Layout';
import { Empty, ErrorBox } from '../components/UI';
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
export function StaffBooking() {
  const { user, catalog, notify } = useApp();
  const [doctor, setDoctor] = useState(String(catalog!.doctors[0]?.id || '')),
    [day, setDay] = useState(catalog!.now.slice(0, 10)),
    [slots, setSlots] = useState<Slot[]>([]),
    [slot, setSlot] = useState(''),
    [error, setError] = useState(''),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    setSlot('');
    const c = new AbortController();
    api(`/api/slots?doctor_id=${doctor}&day=${day}`, 'GET', undefined, c.signal)
      .then((r) => setSlots(r.slots))
      .catch((e) => {
        if (e.name !== 'AbortError') setError(e.message);
      });
    return () => c.abort();
  }, [doctor, day]);
  if (user?.role !== 'receptionist') return <Empty title="Chức năng dành cho lễ tân" />;
  return (
    <>
      <h1>Đặt lịch hộ bệnh nhân</h1>
      <div className="panel content-narrow">
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            const form = e.currentTarget;
            setBusy(true);
            setError('');
            try {
              const r = await api('/api/appointments', 'POST', {
                ...Object.fromEntries(new FormData(form)),
                slot_id: Number(slot),
              });
              notify(`Đã tạo và xác nhận lịch #${r.appointment.id}.`);
              setSlots((x) => x.filter((s) => s.id !== Number(slot)));
              setSlot('');
            } catch (err) {
              setError((err as Error).message);
            } finally {
              setBusy(false);
            }
          }}
        >
          <div className="form-grid">
            <label>
              Bác sĩ
              <select value={doctor} onChange={(e) => setDoctor(e.target.value)}>
                {catalog!.doctors.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name} · {d.specialty_name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Ngày khám
              <input
                type="date"
                min={catalog!.now.slice(0, 10)}
                value={day}
                onChange={(e) => setDay(e.target.value)}
              />
            </label>
            <label>
              Họ tên bệnh nhân
              <input name="patient_name" required maxLength={100} />
            </label>
            <label>
              Điện thoại
              <input name="patient_phone" type="tel" required pattern="[+0-9 ()-]{8,20}" />
            </label>
          </div>
          <label>
            Email tài khoản đã đăng ký (nếu có)
            <input name="patient_email" type="email" />
          </label>
          <label>
            Giờ khám
            <select required value={slot} onChange={(e) => setSlot(e.target.value)}>
              <option value="">Chọn khung giờ</option>
              {slots.map((s) => (
                <option value={s.id} key={s.id}>
                  {s.starts_at.slice(11, 16)} – {s.ends_at.slice(11, 16)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Lý do khám
            <textarea name="reason" required maxLength={500} />
          </label>
          {error && <ErrorBox message={error} />}
          <button className="primary full" disabled={busy || !slot}>
            {busy ? 'Đang tạo…' : 'Tạo lịch và xác nhận'}
          </button>
        </form>
      </div>
    </>
  );
}
