import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { MapPin } from 'lucide-react';
import { useApp } from '../../context';
import { media, doctorImage, specialtyImage } from '../../data';
import { Breadcrumb, Empty } from '../../components/UI';
import Schedule from '../../components/Schedule';

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
                <p>Khám {(d.specialty_name ?? '').toLowerCase()} tại phòng khám CAMS.</p>
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
