import { useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { MapPin, Search } from 'lucide-react';
import { useApp } from '../context';
import { normalize } from '../types';
import { media, doctorImage, specialtyImage } from '../data';
import { Breadcrumb, Empty } from '../components/UI';
import { SpecialtyCard, DoctorCard } from '../components/Cards';
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
