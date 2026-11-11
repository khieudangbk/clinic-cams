import { useEffect } from 'react';
import { Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import { useApp } from './context';
import { Loading, ErrorBox } from './components/UI';
import { PublicLayout } from './components/Layout';
import Home from './pages/Home';
import { Specialties, SpecialtyDetail, DoctorList, DoctorDetail } from './pages/Discovery';
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
    </Routes>
  );
}
