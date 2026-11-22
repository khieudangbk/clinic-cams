import { Link, useParams } from 'react-router-dom';
import { CalendarCheck, CheckCircle2 } from 'lucide-react';
import { services, media } from '../../data';
import { Breadcrumb, Empty } from '../../components/UI';

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
