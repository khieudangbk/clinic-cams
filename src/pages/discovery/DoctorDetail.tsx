import { Link, useParams } from 'react-router-dom';
import { MapPin } from 'lucide-react';
import { useApp } from '../../context';
import { media, doctorImage } from '../../data';
import { Breadcrumb, Empty } from '../../components/UI';
import Schedule from '../../components/Schedule';

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
              Khám chuyên khoa {(d.specialty_name ?? '').toLowerCase()}
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
