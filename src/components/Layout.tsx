import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate, Outlet } from 'react-router-dom';
import {
  Menu,
  History,
  Handshake,
  Search,
  ArrowUp,
  Phone,
  MapPin,
  Mail,
  Plus,
  X,
  LogOut,
  UserRound,
} from 'lucide-react';
import { useApp } from '../context';
import { Modal } from './UI';
import { normalize } from '../types';
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
export function Footer() {
  return (
    <footer>
      <div className="footer-main">
        <div className="container footer-grid">
          <div>
            <Logo />
            <strong className="footer-company">Nền tảng đặt lịch khám CAMS</strong>
            <p>
              <MapPin size={20} /> Phòng khám CAMS · Môi trường trải nghiệm
            </p>
            <p>
              <Mail size={20} /> cams@nano.id.vn
            </p>
          </div>
          <div className="footer-links">
            {[
              ['/hop-tac', 'Liên hệ hợp tác'],
              ['/chuyen-khoa', 'Khám chuyên khoa'],
              ['/huong-dan', 'Quy trình đặt khám'],
              ['/cau-hoi-thuong-gap', 'Câu hỏi thường gặp'],
              ['/quyen-rieng-tu', 'Chính sách quyền riêng tư'],
              ['/nhan-vien', 'Dành cho nhân viên'],
            ].map(([to, label]) => (
              <Link key={to} to={to}>
                {label}
              </Link>
            ))}
          </div>
          <div>
            <h3>CAMS đồng hành cùng bạn</h3>
            <p>Chủ động chọn bác sĩ và khung giờ, theo dõi lịch hẹn trước khi đến khám.</p>
            <Link to="/cam-nang/dat-lich-kham" className="footer-partner">
              <img src="/media/service-specialty.png" alt="" />
              <span>
                Hướng dẫn đi khám
                <br />
                <small>Dễ hiểu, thuận tiện</small>
              </span>
            </Link>
            <Link to="/cam-nang/quyen-rieng-tu" className="footer-partner">
              <img src="/media/service-test.png" alt="" />
              <span>
                Thông tin của bạn
                <br />
                <small>Được phân quyền truy cập</small>
              </span>
            </Link>
          </div>
        </div>
        <div className="container footer-app">
          Đặt lịch khám thuận tiện trên trình duyệt: <Link to="/bac-si">Tìm bác sĩ</Link> –{' '}
          <Link to="/lich-hen">Lịch hẹn của tôi</Link>
        </div>
      </div>
      <div className="footer-bottom">
        <div className="container">
          <span>© 2026 CAMS.</span>
          <span>Thông tin không thay thế tư vấn trực tiếp từ bác sĩ.</span>
        </div>
      </div>
    </footer>
  );
}
export function FloatingTools() {
  const [help, setHelp] = useState(false),
    [contact, setContact] = useState(false),
    [query, setQuery] = useState(''),
    [top, setTop] = useState(false);
  const { catalog } = useApp();
  useEffect(() => {
    const listener = () => setTop(window.scrollY > 400);
    window.addEventListener('scroll', listener);
    return () => window.removeEventListener('scroll', listener);
  }, []);
  const matches =
    catalog?.doctors
      .filter((x) => query && normalize(x.name + ' ' + x.specialty_name).includes(normalize(query)))
      .slice(0, 4) || [];
  return (
    <>
      {top && (
        <button
          className="scroll-top"
          aria-label="Về đầu trang"
          onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
        >
          <ArrowUp />
        </button>
      )}
      <button
        className="contact-float"
        aria-label="Thông tin hỗ trợ"
        onClick={() => setContact(true)}
      >
        <Phone />
      </button>
      <button className="assistant-float" onClick={() => setHelp(true)}>
        <img src="/media/assistant.png" alt="" />
        <span>
          Trợ lý
          <br />
          Đi khám
        </span>
      </button>
      {contact && (
        <Modal title="Hỗ trợ đặt khám" onClose={() => setContact(false)}>
          <p>Tìm hướng dẫn và giải đáp về thao tác đặt lịch trên CAMS.</p>
          <Link to="/huong-dan" className="primary" onClick={() => setContact(false)}>
            Xem hướng dẫn
          </Link>
          <p className="muted">Thông tin liên hệ phòng khám chưa được cung cấp trong bản demo.</p>
        </Modal>
      )}
      {help && (
        <Modal title="Trợ lý tra cứu lịch khám" onClose={() => setHelp(false)}>
          <p>
            Tìm bác sĩ theo tên hoặc chuyên khoa. Đây là công cụ tra cứu, không chẩn đoán hay tư vấn
            y khoa.
          </p>
          <label>
            Nhập nhu cầu tra cứu
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Ví dụ: Tim mạch"
            />
          </label>
          {query && !matches.length && <p>Chưa có kết quả phù hợp. Thử tên chuyên khoa khác.</p>}
          {matches.map((d) => (
            <Link
              className="assistant-result"
              key={d.id}
              to={'/bac-si/' + d.id}
              onClick={() => setHelp(false)}
            >
              <strong>{d.name}</strong>
              <small>{d.specialty_name}</small>
            </Link>
          ))}
          <Link to="/bac-si" onClick={() => setHelp(false)}>
            Xem tất cả bác sĩ →
          </Link>
        </Modal>
      )}
    </>
  );
}
export function PublicLayout() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  return (
    <>
      <a className="skip-link" href="#main">
        Bỏ qua điều hướng
      </a>
      <Header />
      <main id="main">
        <Outlet />
      </main>
      <Footer />
      <FloatingTools />
    </>
  );
}
