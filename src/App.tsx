import { useEffect } from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import { useApp } from './context';
import { Loading, ErrorBox } from './components/UI';
import { PublicLayout } from './components/Layout';
import { Home } from './pages/home/Home';
import { Specialties } from './pages/discovery/Specialties';
import { SpecialtyDetail } from './pages/discovery/SpecialtyDetail';
import { DoctorList } from './pages/discovery/DoctorList';
import { DoctorDetail } from './pages/discovery/DoctorDetail';
import { Facilities } from './pages/discovery/Facilities';
import { FacilityDetail } from './pages/discovery/FacilityDetail';
import { Auth } from './pages/account/Auth';
import { Booking } from './pages/booking/Booking';
import { Appointments } from './pages/appointments/Appointments';
import { Handbook } from './pages/content/Handbook';
import { Article } from './pages/content/Article';
import { Service } from './pages/content/Service';
import { Assistant } from './pages/content/Assistant';
import { Information } from './pages/content/Information';
import { StaffLayout } from './pages/staff/layout/StaffLayout';
import { Dashboard } from './pages/staff/dashboard/Dashboard';
import { StaffAppointments } from './pages/staff/appointments/StaffAppointments';
import { StaffBooking } from './pages/staff/booking/StaffBooking';
import { StaffSchedule } from './pages/staff/schedule/StaffSchedule';
import { Admin } from './pages/staff/admin/Admin';
export default function App() {
  const { ready, error, catalog } = useApp();
  const { pathname } = useLocation();
  const nav = useNavigate();
  useEffect(() => {
    const old = window.location.hash;
    if (old === '#workspace') nav('/nhan-vien', { replace: true });
    else if (old === '#appointments') nav('/lich-hen', { replace: true });
    else if (old === '#home') nav('/', { replace: true });
  }, []);
  useEffect(() => {
    document.title =
      (pathname.startsWith('/nhan-vien')
        ? 'Quản lý phòng khám'
        : pathname.startsWith('/bac-si')
          ? 'Bác sĩ & lịch khám'
          : pathname.startsWith('/lich-hen')
            ? 'Lịch hẹn của tôi'
            : 'Đặt lịch khám trực tuyến') + ' · CAMS';
    window.scrollTo(0, 0);
  }, [pathname]);
  if (!ready) return <Loading />;
  if (error || !catalog)
    return (
      <div className="container page">
        <ErrorBox
          message={error || 'Không tải được danh mục.'}
          retry={() => window.location.reload()}
        />
      </div>
    );
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        {['/', '/tai-nha', '/tai-vien', '/song-khoe'].map((path) => (
          <Route path={path} element={<Home />} key={path} />
        ))}
        <Route path="/chuyen-khoa" element={<Specialties />} />
        <Route path="/chuyen-khoa/:id" element={<SpecialtyDetail />} />
        <Route path="/bac-si" element={<DoctorList />} />
        <Route path="/tim-kiem" element={<DoctorList />} />
        <Route path="/bac-si/:id" element={<DoctorDetail />} />
        <Route path="/dat-lich-kham/:id" element={<Booking />} />
        <Route path="/co-so-y-te" element={<Facilities />} />
        <Route path="/co-so-y-te/cams" element={<FacilityDetail />} />
        <Route path="/dang-nhap" element={<Auth />} />
        <Route path="/lich-hen" element={<Appointments />} />
        <Route path="/cam-nang" element={<Handbook />} />
        <Route path="/cam-nang/:id" element={<Article />} />
        <Route path="/dich-vu/:id" element={<Service />} />
        <Route path="/tro-ly" element={<Assistant />} />
        {['/huong-dan', '/cau-hoi-thuong-gap', '/quyen-rieng-tu', '/hop-tac', '/tai-khoan'].map(
          (path) => (
            <Route path={path} element={<Information />} key={path} />
          ),
        )}
        <Route
          path="*"
          element={
            <div className="container page">
              <h1>Không tìm thấy trang</h1>
              <p>Liên kết có thể đã thay đổi.</p>
              <Link to="/">Về trang chủ</Link>
            </div>
          }
        />
      </Route>
      <Route path="/nhan-vien" element={<StaffLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="lich-hen" element={<StaffAppointments />} />
        <Route path="dat-lich" element={<StaffBooking />} />
        <Route path="lich-bac-si" element={<StaffSchedule />} />
        <Route path="quan-tri" element={<Admin />} />
      </Route>
    </Routes>
  );
}
