import { useEffect, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Search, CheckCircle2, CalendarDays } from 'lucide-react';
import { useApp } from '../context';
import { Section, Carousel } from '../components/UI';
import {
  SpecialtyCard,
  DoctorCard,
  FacilityCard,
  ArticleCard,
  ServiceMediaCard,
} from '../components/Cards';
import { services, media, articles } from '../data';
function Banner() {
  const [slide, setSlide] = useState(0),
    [pause, setPause] = useState(false);
  useEffect(() => {
    if (pause || matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    const timer = setInterval(() => setSlide((x) => (x + 1) % 3), 6500);
    return () => clearInterval(timer);
  }, [pause]);
  return (
    <div
      className="promotion-carousel"
      onMouseEnter={() => setPause(true)}
      onMouseLeave={() => setPause(false)}
      onFocusCapture={() => setPause(true)}
      onBlurCapture={() => setPause(false)}
    >
      <Link to="/bac-si" aria-label="Đặt khám tại CAMS">
        <img
          src={`/media/banner-${slide}.svg`}
          alt={
            [
              'Chủ động lịch khám — Đặt khám ngay',
              'Theo dõi lịch hẹn trong tài khoản',
              'CAMS — Khám theo chuyên khoa',
            ][slide]
          }
        />
      </Link>
      <div className="carousel-dots">
        {[0, 1, 2].map((n) => (
          <button
            key={n}
            aria-label={`Xem banner ${n + 1}`}
            aria-pressed={slide === n}
            className={slide === n ? 'active' : ''}
            onClick={() => setSlide(n)}
          />
        ))}
      </div>
    </div>
  );
}
export default function Home() {
  const { catalog } = useApp();
  const [query, setQuery] = useState('');
  const nav = useNavigate();
  const { pathname } = useLocation();
  if (!catalog) return null;
  const wellness = pathname === '/song-khoe',
    atHome = pathname === '/tai-nha';
  return (
    <>
      <section className="hero-band">
        <div className="container">
          <h1>
            Tìm kiếm bác sĩ, phòng khám, dịch vụ y tế
            <br className="desktop-break" /> và đặt lịch khám
          </h1>
          <form
            className="hero-search"
            role="search"
            onSubmit={(e) => {
              e.preventDefault();
              nav('/tim-kiem?q=' + encodeURIComponent(query));
            }}
          >
            <label className="sr-only" htmlFor="hero-query">
              Tìm bác sĩ, chuyên khoa
            </label>
            <input
              id="hero-query"
              placeholder="Tìm bác sĩ, chuyên khoa, phòng khám…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              maxLength={100}
            />
            <button aria-label="Tìm kiếm">
              <Search />
            </button>
          </form>
          <h2 className="assistant-title">Tra cứu & hỗ trợ đặt khám</h2>
          <Link to="/tro-ly" className="ai-card">
            <img src="/media/assistant.png" alt="" />
            <div>
              <h3>Trợ lý Đi khám</h3>
              <p>Tìm kiếm thông tin bác sĩ, nơi khám và đặt lịch khám</p>
            </div>
          </Link>
        </div>
      </section>
      {atHome && (
        <div className="container category-notice">
          <strong>Dịch vụ tại nhà</strong>
          <p>
            CAMS hiện hỗ trợ đặt lịch khám trực tiếp. Các dịch vụ tại nhà và từ xa đang ở phần giới
            thiệu trong bản trải nghiệm.
          </p>
          <Link to="/bac-si">Xem lịch khám tại phòng khám →</Link>
        </div>
      )}
      <Section
        title={wellness ? 'Thông tin cho hành trình chăm sóc sức khỏe' : 'Dịch vụ toàn diện'}
      >
        <div className="service-grid">
          {services.map(([label, image, to]) => (
            <Link key={label} className="service-tile" to={to}>
              <span className="service-halo">
                <img loading="lazy" src={media + image} alt="" />
              </span>
              <strong>{label}</strong>
            </Link>
          ))}
        </div>
      </Section>
      <Section title="Chuyên khoa" to="/chuyen-khoa">
        <Carousel label="Chuyên khoa">
          {catalog.specialties.map((s) => (
            <SpecialtyCard key={s.id} specialty={s} />
          ))}
        </Carousel>
      </Section>
      <Section title="Cơ sở y tế" to="/co-so-y-te">
        <Carousel label="Cơ sở y tế">
          <FacilityCard />
          <Link to="/co-so-y-te/cams" className="facility-info-card">
            <CalendarDays />
            <h3>Đặt lịch tại phòng khám CAMS</h3>
            <p>
              {catalog.specialties.length} chuyên khoa · {catalog.doctors.length} bác sĩ
            </p>
            <span>Chọn bác sĩ và khung giờ phù hợp →</span>
          </Link>
        </Carousel>
      </Section>
      <Section title="Đặt khám thuận tiện cùng CAMS">
        <Banner />
      </Section>
      <Section title="Bác sĩ nổi bật" to="/bac-si" className="doctors-band">
        <Carousel variant="doctors" label="Bác sĩ nổi bật">
          {catalog.doctors.map((d) => (
            <DoctorCard key={d.id} doctor={d} />
          ))}
        </Carousel>
      </Section>
      <Section title="Khám từ xa" to="/dich-vu/kham-tu-xa">
        <Carousel label="Khám từ xa">
          {[
            'Tư vấn, trị liệu Tâm lý từ xa',
            'Bác sĩ Da liễu từ xa',
            'Bác sĩ Tim mạch từ xa',
            'Bác sĩ Nội tổng quát từ xa',
          ].map((title, i) => (
            <ServiceMediaCard
              key={title}
              title={title}
              to="/dich-vu/kham-tu-xa"
              image={
                [
                  'article-privacy.svg',
                  'specialty-skin.svg',
                  'specialty-heart.png',
                  'specialty-digestive.png',
                ][i]
              }
              video
            />
          ))}
        </Carousel>
        <p className="section-note">
          Thông tin dịch vụ tham khảo. Bản CAMS hiện chỉ nhận đặt khám trực tiếp.
        </p>
      </Section>
      <Section title="Gợi ý của CAMS">
        <Carousel variant="doctors" label="Gợi ý đặt khám">
          {catalog.specialties.map((s) => (
            <Link className="doctor-card suggestion" to={'/chuyen-khoa/' + s.id} key={s.id}>
              <img
                loading="lazy"
                src={
                  media +
                  (s.name === 'Tim mạch'
                    ? 'specialty-heart.png'
                    : s.name === 'Da liễu'
                      ? 'specialty-skin.svg'
                      : 'specialty-digestive.png')
                }
                alt=""
              />
              <h3>Khám {s.name.toLowerCase()}</h3>
            </Link>
          ))}
        </Carousel>
      </Section>
      <Section
        title="Sức khỏe tinh thần"
        to="/dich-vu/suc-khoe-tinh-than"
        className="wellness-band"
      >
        <Carousel label="Sức khỏe tinh thần">
          {[
            ['Thông tin trước khi đi khám', 'article-prepare.svg'],
            ['Chủ động sắp xếp lịch hẹn', 'article-calendar.svg'],
            ['Trao đổi với cơ sở y tế', 'article-privacy.svg'],
          ].map(([title, image]) => (
            <ServiceMediaCard
              key={title}
              title={title}
              image={image}
              to="/dich-vu/suc-khoe-tinh-than"
            />
          ))}
        </Carousel>
      </Section>
      <Section title="Hỏi đáp trước khi đi khám" to="/cau-hoi-thuong-gap">
        <Carousel label="Hỏi đáp">
          <ServiceMediaCard
            title="Đặt lịch khám trực tuyến như thế nào?"
            image="article-booking.svg"
            to="/cam-nang/dat-lich-kham"
          />
          <ServiceMediaCard
            title="Tôi có thể đổi hoặc hủy lịch không?"
            image="article-calendar.svg"
            to="/cam-nang/doi-huy-lich"
          />
        </Carousel>
      </Section>
      <Section title="Cẩm nang" to="/cam-nang">
        <Carousel variant="articles" label="Cẩm nang">
          {articles.map((a) => (
            <ArticleCard key={a.id} article={a} />
          ))}
        </Carousel>
      </Section>
      <Section title="Chủ động chăm sóc sức khỏe" to="/cam-nang">
        <Carousel variant="articles" label="Chuẩn bị đi khám">
          {[...articles].reverse().map((a) => (
            <ArticleCard key={a.id} article={a} />
          ))}
        </Carousel>
      </Section>
      <Section title="CAMS đồng hành cùng bạn" className="press-band">
        <div className="about-platform">
          <Link className="platform-visual" to="/huong-dan">
            <img src="/media/clinic.svg" alt="Phòng khám CAMS" />
            <strong>Đặt lịch dễ dàng · Theo dõi thuận tiện</strong>
          </Link>
          <div className="benefit-grid">
            {[
              'Chọn bác sĩ phù hợp',
              'Chủ động khung giờ',
              'Theo dõi xác nhận',
              'Quản lý lịch cá nhân',
            ].map((t) => (
              <Link key={t} to="/huong-dan">
                <CheckCircle2 />
                {t}
              </Link>
            ))}
          </div>
        </div>
      </Section>
      <Section title="Dành cho bác sĩ và cơ sở y tế" to="/hop-tac">
        <Carousel variant="articles" label="Dành cho cơ sở y tế">
          {articles.slice(0, 3).map((a) => (
            <ArticleCard key={a.id} article={a} />
          ))}
        </Carousel>
      </Section>
    </>
  );
}
