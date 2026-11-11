import { useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { MapPin, Search } from 'lucide-react';
import { useApp } from '../context';
import { normalize } from '../types';
import { media, doctorImage, specialtyImage } from '../data';
import { Breadcrumb, Empty } from '../components/UI';
import { SpecialtyCard, DoctorCard, FacilityCard } from '../components/Cards';
import Schedule from '../components/Schedule';
export function Specialties() {
  const { catalog } = useApp();
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Khám Chuyên khoa' }]} />
      <h1 className="listing-heading">Khám Chuyên khoa</h1>
      <div className="listing-grid">
        {catalog!.specialties.map((s) => (
          <SpecialtyCard key={s.id} specialty={s} />
        ))}
      </div>
    </div>
  );
}
export function DoctorList() {
  const { catalog } = useApp();
  const [params, setParams] = useSearchParams();
  const [query, setQuery] = useState(params.get('q') || '');
  const specialty = params.get('specialty') || '';
  const doctors = catalog!.doctors.filter(
    (d) =>
      (!specialty || String(d.specialty_id) === specialty) &&
      normalize(d.name + ' ' + d.specialty_name).includes(normalize(params.get('q') || '')),
  );
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Tìm bác sĩ' }]} />
      <h1>Tìm kiếm bác sĩ, chuyên khoa</h1>
      <form
        className="search-filters"
        onSubmit={(e) => {
          e.preventDefault();
          setParams({ q: query, ...(specialty ? { specialty } : {}) });
        }}
      >
        <label>
          <span className="sr-only">Tìm kiếm</span>
          <input
            placeholder="Tên bác sĩ hoặc chuyên khoa…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </label>
        <label>
          <span className="sr-only">Chuyên khoa</span>
          <select
            value={specialty}
            onChange={(e) =>
              setParams({ q: query, ...(e.target.value ? { specialty: e.target.value } : {}) })
            }
          >
            <option value="">Tất cả chuyên khoa</option>
            {catalog!.specialties.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </label>
        <button className="primary">
          <Search size={18} />
          Tìm kiếm
        </button>
        <button
          type="button"
          className="outline"
          onClick={() => {
            setQuery('');
            setParams({});
          }}
        >
          Xóa bộ lọc
        </button>
      </form>
      <p className="result-count" role="status">
        Tìm thấy {doctors.length} bác sĩ
      </p>
      {doctors.length ? (
        <div className="listing-grid doctor-list">
          {doctors.map((d) => (
            <DoctorCard key={d.id} doctor={d} />
          ))}
        </div>
      ) : (
        <Empty title="Không tìm thấy bác sĩ">
          <p>Thử tên hoặc chuyên khoa khác.</p>
        </Empty>
      )}
    </div>
  );
}
export function SpecialtyDetail() {
  const { id } = useParams();
  const { catalog } = useApp();
  const [expanded, setExpanded] = useState(false);
  const s = catalog!.specialties.find((x) => x.id === Number(id));
  if (!s)
    return (
      <Empty title="Không tìm thấy chuyên khoa">
        <Link to="/chuyen-khoa">Xem danh mục</Link>
      </Empty>
    );
  const doctors = catalog!.doctors.filter((d) => d.specialty_id === s.id);
  return (
    <div className="specialty-detail">
      <div
        className="specialty-intro"
        style={{
          backgroundImage: `linear-gradient(to bottom,rgba(255,255,255,.85),#fff),url(${media + specialtyImage(s)})`,
        }}
      >
        <div className="container">
          <Breadcrumb
            items={[{ label: 'Khám Chuyên khoa', to: '/chuyen-khoa' }, { label: s.name }]}
          />
          <h1>{s.name}</h1>
          <p>
            Đặt lịch khám {s.name.toLowerCase()} tại phòng khám CAMS. Lựa chọn bác sĩ và khung giờ
            phù hợp với lịch của bạn.
          </p>
          {expanded && (
            <p>
              Các lịch còn trống được cập nhật từ hệ thống. Yêu cầu đặt trực tuyến sẽ được lễ tân
              kiểm tra và xác nhận. Thông tin bác sĩ trong bản demo là giả lập; không thay thế tư
              vấn y khoa.
            </p>
          )}
          <button className="text-link" onClick={() => setExpanded(!expanded)}>
            {expanded ? 'Thu gọn' : 'Xem thêm'}
          </button>
        </div>
      </div>
      <div className="container page">
        <label className="region-filter">
          Cơ sở khám
          <select aria-label="Cơ sở khám">
            <option>Phòng khám CAMS</option>
          </select>
        </label>
        {doctors.map((d) => (
          <article className="doctor-booking-card" key={d.id}>
            <div className="doctor-booking-intro">
              <Link to={'/bac-si/' + d.id}>
                <img src={media + doctorImage(d)} alt="" />
                <span>Xem thêm</span>
              </Link>
              <div>
                <Link to={'/bac-si/' + d.id}>
                  <h2>{d.name}</h2>
                </Link>
                <p>Khám {d.specialty_name.toLowerCase()} tại phòng khám CAMS.</p>
                <p>
                  <MapPin size={15} /> Cơ sở demo
                </p>
              </div>
            </div>
            <Schedule doctor={d} compact />
          </article>
        ))}
      </div>
    </div>
  );
}
export function DoctorDetail() {
  const { id } = useParams();
  const { catalog } = useApp();
  const d = catalog!.doctors.find((x) => x.id === Number(id));
  if (!d)
    return (
      <Empty title="Bác sĩ không còn trong danh mục">
        <Link to="/bac-si">Chọn bác sĩ khác</Link>
      </Empty>
    );
  return (
    <>
      <div className="container page doctor-detail">
        <Breadcrumb items={[{ label: 'Bác sĩ', to: '/bac-si' }, { label: d.name }]} />
        <section className="doctor-intro">
          <img src={media + doctorImage(d)} alt={'Minh họa ' + d.name} />
          <div>
            <h1>{d.name}</h1>
            <p>
              Khám chuyên khoa {d.specialty_name.toLowerCase()}
              <br />
              Đặt lịch khám trực tiếp tại phòng khám CAMS
            </p>
            <p>
              <MapPin size={15} /> Phòng khám CAMS
            </p>
            <Link to="/tro-ly" className="outline-pill">
              Hỏi thêm về đặt lịch
            </Link>
          </div>
        </section>
        <Schedule doctor={d} />
      </div>
      <section className="doctor-biography">
        <div className="container">
          <h2>Thông tin khám</h2>
          <h3>Chuyên khoa</h3>
          <p>
            {d.specialty_name}. Bản trải nghiệm không công bố chứng chỉ, kinh nghiệm hoặc thông tin
            hành nghề thực tế.
          </p>
          <h3>Quy trình đặt khám</h3>
          <p>
            Chọn ngày và giờ trống, đăng nhập, điền thông tin và gửi yêu cầu. Theo dõi xác nhận
            trong Lịch hẹn.
          </p>
          <h2>Phản hồi sau khi đi khám</h2>
          <p className="muted">Chưa có phản hồi được xác minh trong bản demo.</p>
        </div>
      </section>
      <div className="faq-strip">
        <div className="container">
          Bạn cần giải đáp trước khi đặt khám?{' '}
          <Link to="/cau-hoi-thuong-gap">Xem câu hỏi thường gặp.</Link>
        </div>
      </div>
    </>
  );
}
export function Facilities() {
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Cơ sở y tế' }]} />
      <h1>Cơ sở y tế</h1>
      <p>Đặt khám trực tiếp tại cơ sở đang được hỗ trợ trong dự án.</p>
      <div className="listing-grid">
        <FacilityCard />
      </div>
    </div>
  );
}
export function FacilityDetail() {
  return (
    <>
      <div className="facility-cover">
        <img src="/media/clinic.svg" alt="Phòng khám CAMS" />
      </div>
      <div className="container">
        <div className="facility-header">
          <img src="/media/clinic.svg" alt="" />
          <div>
            <h1>Phòng khám đa khoa CAMS</h1>
            <p>Cơ sở giả lập của dự án CAMS</p>
          </div>
        </div>
        <nav className="facility-tabs">
          {[
            ['gioi-thieu', 'Giới thiệu'],
            ['chuyen-mon', 'Chuyên môn'],
            ['quy-trinh', 'Quy trình khám'],
          ].map(([id, name]) => (
            <a href={'#' + id} key={id}>
              {name}
            </a>
          ))}
        </nav>
        <div className="facility-content">
          <Breadcrumb
            items={[{ label: 'Cơ sở y tế', to: '/co-so-y-te' }, { label: 'Phòng khám CAMS' }]}
          />
          <p className="notice">
            Đây là cơ sở minh họa. Chưa có địa chỉ và phí khám thực tế; không dùng để đặt khám ngoài
            dự án.
          </p>
          <section id="gioi-thieu">
            <h2>Giới thiệu</h2>
            <p>
              CAMS hỗ trợ tìm kiếm bác sĩ và quản lý lịch hẹn từ đặt lịch, xác nhận đến tiếp nhận
              tại phòng khám. Lịch mẫu hoạt động 07:00–17:00, thứ Hai đến thứ Bảy; giờ trống thực tế
              tùy từng bác sĩ.
            </p>
            <img
              className="facility-body-image"
              src="/media/clinic.svg"
              alt="Minh họa cơ sở khám"
            />
          </section>
          <section id="chuyen-mon">
            <h2>Chuyên môn</h2>
            <p>Tra cứu các chuyên khoa đang hoạt động và chọn bác sĩ phù hợp trong danh mục.</p>
            <Link to="/chuyen-khoa" className="outline-pill">
              Xem chuyên khoa
            </Link>
          </section>
          <section id="quy-trinh">
            <h2>Quy trình khám</h2>
            <ol>
              <li>Tìm bác sĩ, chọn ngày và giờ trống.</li>
              <li>Đăng nhập và xác nhận thông tin người khám.</li>
              <li>Theo dõi xác nhận, cung cấp mã hẹn khi đến lễ tân.</li>
            </ol>
            <Link to="/bac-si" className="primary">
              Chọn bác sĩ và đặt khám
            </Link>
          </section>
        </div>
      </div>
    </>
  );
}
