import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useApp } from '../../context';
import { articles, faqs } from '../../data';
import { Breadcrumb } from '../../components/UI';

export function Information() {
  const { pathname } = useLocation();
  const { user, logout, notify } = useApp();
  const nav = useNavigate();
  if (pathname === '/tai-khoan')
    return (
      <div className="container page auth-card">
        <h1>Tài khoản của tôi</h1>
        {user ? (
          <>
            <p>
              <strong>{user.name}</strong>
            </p>
            <p>{user.email}</p>
            <p>{user.phone}</p>
            <Link to={user.role === 'patient' ? '/lich-hen' : '/nhan-vien'} className="primary">
              {user.role === 'patient' ? 'Lịch hẹn của tôi' : 'Khu vực nhân viên'}
            </Link>
            <button
              className="outline"
              onClick={async () => {
                try {
                  await logout();
                  nav('/');
                } catch (e) {
                  notify((e as Error).message, true);
                }
              }}
            >
              Đăng xuất
            </button>
          </>
        ) : (
          <Link to="/dang-nhap" className="primary">
            Đăng nhập
          </Link>
        )}
      </div>
    );
  if (pathname === '/quyen-rieng-tu')
    return (
      <div className="container page content-narrow">
        <Breadcrumb items={[{ label: 'Quyền riêng tư' }]} />
        <h1>Thông tin quyền riêng tư</h1>
        {articles[3].sections.map(([t, d]) => (
          <section key={t}>
            <h2>{t}</h2>
            <p>{d}</p>
          </section>
        ))}
      </div>
    );
  if (pathname === '/hop-tac')
    return (
      <div className="container page content-narrow">
        <Breadcrumb items={[{ label: 'Hợp tác' }]} />
        <h1>Dành cho bác sĩ và cơ sở y tế</h1>
        <p>CAMS hỗ trợ quản lý lịch khám, phân quyền nhân viên, tiếp nhận và theo dõi lịch hẹn.</p>
        <div className="public-info-cards">
          <Link to="/nhan-vien">Đăng nhập khu vực nhân viên →</Link>
          <Link to="/co-so-y-te/cams">Thông tin phòng khám →</Link>
        </div>
        <p className="notice">
          Bản dự án học tập chưa có kênh tiếp nhận đăng ký hợp tác bên ngoài. Không gửi thông tin cá
          nhân hoặc hồ sơ thật.
        </p>
      </div>
    );
  return (
    <div className="container page content-narrow">
      <Breadcrumb
        items={[{ label: pathname === '/huong-dan' ? 'Hướng dẫn đặt khám' : 'Câu hỏi thường gặp' }]}
      />
      <h1>{pathname === '/huong-dan' ? 'Hướng dẫn đặt khám' : 'Câu hỏi thường gặp'}</h1>
      {pathname === '/huong-dan' && (
        <div className="guide-steps">
          {[
            ['1', 'Tìm bác sĩ', 'Tìm theo tên hoặc chọn chuyên khoa ngay trên trang chủ.'],
            [
              '2',
              'Chọn ngày và giờ',
              'Xem lịch trống, chọn khung giờ phù hợp trước khi đăng nhập.',
            ],
            ['3', 'Xác nhận yêu cầu', 'Đăng nhập, nhập thông tin người khám và lý do khám.'],
            [
              '4',
              'Theo dõi lịch và đến khám',
              'Kiểm tra xác nhận ở Lịch hẹn và cung cấp mã hẹn cho lễ tân.',
            ],
          ].map(([n, t, d]) => (
            <article key={n}>
              <span>{n}</span>
              <div>
                <h2>{t}</h2>
                <p>{d}</p>
              </div>
            </article>
          ))}
        </div>
      )}
      <div className="faq-list">
        {faqs.map(([q, a]) => (
          <details key={q}>
            <summary>{q}</summary>
            <p>{a}</p>
          </details>
        ))}
      </div>
      <Link to="/bac-si" className="primary">
        Bắt đầu đặt khám
      </Link>
    </div>
  );
}
