import { Link, useParams, useLocation, useNavigate } from 'react-router-dom';
import { CalendarCheck, CheckCircle2 } from 'lucide-react';
import { useApp } from '../context';
import { articles, services, faqs, media } from '../data';
import { Breadcrumb, Carousel, Empty } from '../components/UI';
import { ArticleCard } from '../components/Cards';
export function Handbook() {
  return (
    <div className="container page handbook">
      <Breadcrumb items={[{ label: 'Cẩm nang' }]} />
      <h1>Bài viết mới nhất</h1>
      <Carousel label="Bài viết mới nhất" variant="articles">
        {articles.map((a) => (
          <ArticleCard article={a} key={a.id} indexStyle />
        ))}
      </Carousel>
      {['Hướng dẫn đi khám', 'Sử dụng CAMS'].map((category) => (
        <section key={category}>
          <h2>{category}</h2>
          <div className="handbook-grid">
            {articles
              .filter((a) => a.category === category)
              .map((a) => (
                <ArticleCard article={a} key={a.id} indexStyle />
              ))}
          </div>
        </section>
      ))}
    </div>
  );
}
export function Article() {
  const { id } = useParams();
  const article = articles.find((x) => x.id === id);
  if (!article)
    return (
      <Empty title="Không tìm thấy bài viết">
        <Link to="/cam-nang">Về cẩm nang</Link>
      </Empty>
    );
  return (
    <div className="article-page">
      <div className="article-cover">
        <img src={media + article.image} alt="" />
        <div className="container">
          <Breadcrumb items={[{ label: 'Cẩm nang', to: '/cam-nang' }, { label: article.title }]} />
        </div>
      </div>
      <div className="container article-layout">
        <article className="article-body">
          <figure>
            <img src={media + article.image} alt="Minh họa nội dung bài viết" />
            <figcaption>Minh họa hướng dẫn sử dụng CAMS</figcaption>
          </figure>
          <h1>{article.title}</h1>
          <p className="article-meta">
            Biên soạn: <Link to="/huong-dan">Nhóm CAMS</Link> · Nội dung hướng dẫn demo
          </p>
          <p>{article.intro}</p>
          <blockquote>
            Chủ động đặt lịch, kiểm tra xác nhận và chuẩn bị trước khi đến khám.
          </blockquote>
          {article.sections.map(([title, body], i) => (
            <section id={'muc-' + i} key={title}>
              <h2>{title}</h2>
              <p>{body}</p>
            </section>
          ))}
          <p className="article-note">
            Bài viết hướng dẫn thao tác và chuẩn bị chung, không thay thế chỉ định hay tư vấn của
            bác sĩ.
          </p>
          <Link to="/bac-si" className="outline-pill">
            Tìm bác sĩ và đặt lịch
          </Link>
        </article>
        <aside className="article-sidebar">
          <div>
            <h2>Nội dung chính</h2>
            {article.sections.map(([title], i) => (
              <a key={title} href={'#muc-' + i}>
                {title}
              </a>
            ))}
          </div>
          <Link className="article-chat" to="/tro-ly">
            <img src="/media/assistant.png" alt="" />
            <strong>Trò chuyện với Trợ lý Đi khám</strong>
          </Link>
        </aside>
      </div>
    </div>
  );
}
export function Service() {
  const { id } = useParams();
  const service = services.find((x) => x[2] === '/dich-vu/' + id);
  if (!service)
    return (
      <Empty title="Không tìm thấy dịch vụ">
        <Link to="/">Trang chủ</Link>
      </Empty>
    );
  const supported = ['kham-tong-quat', 'tieu-duong'].includes(id || '');
  return (
    <div className="container page service-page">
      <Breadcrumb items={[{ label: 'Dịch vụ', to: '/' }, { label: service[0] }]} />
      <img className="service-page-icon" src={media + service[1]} alt="" />
      <h1>{service[0]}</h1>
      <p>
        {supported
          ? 'Bạn có thể bắt đầu bằng việc chọn bác sĩ trong danh mục và mô tả nhu cầu khi đặt lịch.'
          : 'Danh mục giới thiệu theo thiết kế website. Dịch vụ này chưa được vận hành trong bản CAMS hiện tại.'}
      </p>
      <div className="notice">
        <strong>Phạm vi bản trải nghiệm</strong>
        <p>
          CAMS nhận đặt khám trực tiếp theo chuyên khoa. Không thực hiện gọi video, chẩn đoán, thanh
          toán trực tuyến hoặc bài test y tế.
        </p>
      </div>
      <Link className="primary" to="/bac-si">
        Xem bác sĩ đang nhận lịch
      </Link>
      <div className="public-info-cards">
        <Link to="/huong-dan">
          <CalendarCheck />
          Hướng dẫn đặt khám
        </Link>
        <Link to="/cam-nang">
          <CheckCircle2 />
          Chuẩn bị trước buổi khám
        </Link>
      </div>
    </div>
  );
}
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
