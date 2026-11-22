import { Link } from 'react-router-dom';
import { Breadcrumb } from '../../components/UI';

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
