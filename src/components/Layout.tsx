import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { Menu, History, Handshake, Search, Plus, X, LogOut, UserRound } from 'lucide-react';
import { useApp } from '../context';
export function Logo() {
  return (
    <Link to="/" className="logo" aria-label="CAMS trang chủ">
      <span className="logo-mark">
        <Plus strokeWidth={4} />
      </span>
      <span>
        CAMS<small>ĐẶT LỊCH KHÁM TRỰC TUYẾN</small>
      </span>
    </Link>
  );
}
export function Header() {
  const { user, logout, notify } = useApp();
  const { pathname } = useLocation();
  const nav = useNavigate();
  const [drawer, setDrawer] = useState(false);
  const ref = useRef<HTMLDialogElement>(null);
  const home = ['/', '/tai-nha', '/tai-vien', '/song-khoe'].includes(pathname);
  useEffect(() => {
    setDrawer(false);
  }, [pathname]);
  useEffect(() => {
    if (drawer) ref.current?.showModal();
    else ref.current?.close();
  }, [drawer]);
  return (
    <>
      <div className="smart-banner">
        <img src="/media/favicon.svg" alt="" />
        <div>
          <strong>CAMS</strong>
          <small>Đặt khám thuận tiện trên website</small>
        </div>
        <Link to="/bac-si">Mở</Link>
      </div>
      <header className={'site-header ' + (home ? 'home-header' : 'inner-header')}>
        <div className="header-container">
          <button className="menu-button" aria-label="Menu" onClick={() => setDrawer(true)}>
            <Menu />
          </button>
          <Logo />
          {home ? (
            <nav className="category-tabs" aria-label="Danh mục dịch vụ">
              {[
                ['/', 'Tất cả'],
                ['/tai-nha', 'Tại nhà'],
                ['/tai-vien', 'Tại viện'],
                ['/song-khoe', 'Sống khỏe'],
              ].map(([to, label]) => (
                <NavLink end to={to} key={to}>
                  {label}
                </NavLink>
              ))}
            </nav>
          ) : (
            <nav className="inner-nav">
              {[
                ['/chuyen-khoa', 'Chuyên khoa', 'Tìm bác sĩ theo chuyên khoa'],
                ['/co-so-y-te', 'Cơ sở y tế', 'Chọn bệnh viện phòng khám'],
                ['/bac-si', 'Bác sĩ', 'Chọn bác sĩ phù hợp'],
                ['/dich-vu/kham-tong-quat', 'Gói khám', 'Khám sức khỏe tổng quát'],
              ].map(([to, title, sub]) => (
                <Link key={to} to={to}>
                  <strong>{title}</strong>
                  <small>{sub}</small>
                </Link>
              ))}
            </nav>
          )}
          <div className="header-actions">
            {home ? (
              <Link className="cooperate" to="/hop-tac">
                <Handshake />
                <span>Hợp tác</span>
              </Link>
            ) : (
              <Link className="search-action" to="/tim-kiem">
                <Search />
                <span>Tìm kiếm</span>
              </Link>
            )}
            <Link to="/lich-hen">
              <History />
              <span>Lịch hẹn</span>
            </Link>
            <Link className="account-action" to={user ? '/tai-khoan' : '/dang-nhap'}>
              <UserRound />
              <span>{user ? 'Tài khoản' : 'Đăng nhập'}</span>
            </Link>
          </div>
        </div>
      </header>
      <dialog
        ref={ref}
        className="drawer"
        aria-label="Menu điều hướng"
        onCancel={() => setDrawer(false)}
        onClick={(e) => {
          if (e.target === ref.current) setDrawer(false);
        }}
      >
        <div className="drawer-title">
          <Logo />
          <button onClick={() => setDrawer(false)} aria-label="Đóng menu">
            <X />
          </button>
        </div>
        {[
          ['/', 'Trang chủ'],
          ['/chuyen-khoa', 'Chuyên khoa'],
          ['/bac-si', 'Bác sĩ'],
          ['/co-so-y-te', 'Cơ sở y tế'],
          ['/cam-nang', 'Cẩm nang'],
          ['/lich-hen', 'Lịch hẹn của tôi'],
          ['/hop-tac', 'Liên hệ hợp tác'],
        ].map(([to, label]) => (
          <Link key={to} to={to}>
            {label}
          </Link>
        ))}
        <p>Dành cho bạn</p>
        {[
          ['/huong-dan', 'Hướng dẫn đặt khám'],
          ['/cau-hoi-thuong-gap', 'Câu hỏi thường gặp'],
          ['/quyen-rieng-tu', 'Quyền riêng tư'],
          ['/nhan-vien', 'Dành cho nhân viên'],
        ].map(([to, label]) => (
          <Link key={to} to={to}>
            {label}
          </Link>
        ))}
        {user && (
          <button
            className="drawer-logout"
            onClick={async () => {
              try {
                await logout();
                setDrawer(false);
                nav('/');
              } catch (e) {
                notify((e as Error).message, true);
              }
            }}
          >
            <LogOut size={17} />
            Đăng xuất
          </button>
        )}
        <div className="drawer-note">
          Bản trải nghiệm CAMS. Dữ liệu y tế và tài khoản trong demo là giả lập.
        </div>
      </dialog>
    </>
  );
}
