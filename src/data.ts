import type { Doctor, Specialty } from './types';
export const media = '/media/';
export const specialtyImage = (s: Specialty | string) => {
  const name = typeof s === 'string' ? s : s.name;
  return /tim/i.test(name)
    ? 'specialty-heart.png'
    : /da liễu/i.test(name)
      ? 'specialty-skin.svg'
      : 'specialty-digestive.png';
};
export const doctorImage = (d: Doctor) => `doctor-${d.id % 3}.svg`;
export const services = [
  ['Khám Chuyên khoa', 'service-specialty.png', '/chuyen-khoa'],
  ['Khám từ xa', 'service-video.png', '/dich-vu/kham-tu-xa'],
  ['Khám tổng quát', 'service-general.png', '/dich-vu/kham-tong-quat'],
  ['Xét nghiệm y học', 'service-lab.png', '/dich-vu/xet-nghiem'],
  ['Sức khỏe tinh thần', 'service-mental.png', '/dich-vu/suc-khoe-tinh-than'],
  ['Khám nha khoa', 'service-dental.png', '/dich-vu/nha-khoa'],
  ['Gói Phẫu thuật', 'service-surgery.png', '/dich-vu/phau-thuat'],
  ['Sống khỏe Tiểu đường', 'service-diabetes.png', '/dich-vu/tieu-duong'],
  ['Bài Test Sức khỏe', 'service-test.png', '/dich-vu/bai-test'],
  ['Y tế gần bạn', 'service-near.png', '/co-so-y-te'],
];
export const articles = [
  {
    id: 'dat-lich-kham',
    title: 'Đặt lịch khám trực tuyến: hướng dẫn từng bước',
    image: 'article-booking.svg',
    category: 'Hướng dẫn đi khám',
    intro: 'Chủ động chọn bác sĩ và khung giờ trước khi đến phòng khám.',
    sections: [
      [
        'Tìm bác sĩ phù hợp',
        'Từ trang chủ, nhập tên bác sĩ hoặc chuyên khoa. Bạn có thể xem thông tin và lịch trống mà chưa cần đăng nhập.',
      ],
      [
        'Chọn khung giờ và xác nhận',
        'Chọn ngày khám, khung giờ và đăng nhập tài khoản. Điền thông tin người khám, lý do rồi xác nhận. Slot chỉ được giữ sau khi yêu cầu tạo thành công.',
      ],
      [
        'Theo dõi lịch hẹn',
        'Mở Lịch hẹn để xem trạng thái. Yêu cầu trực tuyến cần lễ tân xác nhận trước khi đến khám.',
      ],
    ],
  },
  {
    id: 'chuan-bi-di-kham',
    title: 'Chuẩn bị gì trước khi đến phòng khám?',
    image: 'article-prepare.svg',
    category: 'Hướng dẫn đi khám',
    intro: 'Một vài bước chuẩn bị để buổi khám diễn ra thuận tiện hơn.',
    sections: [
      [
        'Kiểm tra thông tin hẹn',
        'Xác nhận bác sĩ, ngày, giờ và trạng thái lịch hẹn trong tài khoản. Lưu mã lịch để cung cấp cho lễ tân.',
      ],
      [
        'Mang theo giấy tờ cần thiết',
        'Chuẩn bị giấy tờ nhận diện, các kết quả khám trước đó nếu có và thông tin thuốc đang sử dụng để trao đổi trực tiếp với bác sĩ.',
      ],
      [
        'Lưu ý theo hướng dẫn của cơ sở',
        'Việc nhịn ăn hoặc chuẩn bị chuyên biệt chỉ thực hiện theo hướng dẫn của cơ sở y tế; không tự áp dụng dựa trên nội dung chung.',
      ],
    ],
  },
  {
    id: 'doi-huy-lich',
    title: 'Cách đổi, hủy và theo dõi lịch khám trên CAMS',
    image: 'article-calendar.svg',
    category: 'Sử dụng CAMS',
    intro: 'Quản lý lịch hẹn ngay trong tài khoản cá nhân.',
    sections: [
      [
        'Đổi lịch khám',
        'Mở Lịch hẹn, chọn Đổi lịch rồi chọn khung giờ mới và nhập lý do. Hệ thống kiểm tra thời hạn cùng giới hạn số lần đổi trước khi lưu.',
      ],
      [
        'Hủy lịch khi không thể đến',
        'Chọn Hủy lịch, nhập lý do và xác nhận. Lịch bị hủy giải phóng khung giờ cho người khác.',
      ],
      [
        'Xem lịch sử xử lý',
        'Mục Lịch sử thể hiện các lần tạo, xác nhận, đổi, hủy và tiếp nhận với người thực hiện và thời gian.',
      ],
    ],
  },
  {
    id: 'quyen-rieng-tu',
    title: 'Thông tin cá nhân của bạn được sử dụng thế nào?',
    image: 'article-privacy.svg',
    category: 'Sử dụng CAMS',
    intro: 'Hiểu rõ phạm vi sử dụng dữ liệu trong bản trải nghiệm.',
    sections: [
      [
        'Dữ liệu phục vụ lịch khám',
        'CAMS ghi nhận thông tin tài khoản, người khám, bác sĩ, khung giờ và lý do khám. Không có chức năng lưu bệnh án hay thanh toán trực tuyến.',
      ],
      [
        'Quyền truy cập',
        'Bệnh nhân chỉ xem lịch do tài khoản mình đặt. Bác sĩ xem lịch mình phụ trách. Lễ tân và quản lý xử lý lịch hẹn trong phòng khám.',
      ],
      [
        'Bản trải nghiệm cục bộ',
        'Dự án sử dụng dữ liệu giả lập. Hãy dùng thông tin thử nghiệm; chính sách vận hành thực tế cần được xác nhận trước khi triển khai.',
      ],
    ],
  },
];
export const faqs = [
  [
    'Có cần đăng nhập để tìm bác sĩ không?',
    'Không. Bạn có thể tìm chuyên khoa, bác sĩ và xem giờ trống trước khi đăng nhập. Tài khoản chỉ cần khi gửi yêu cầu đặt khám hoặc xem lịch cá nhân.',
  ],
  [
    'Khi nào lịch hẹn được xác nhận?',
    'Yêu cầu trực tuyến có trạng thái Chờ xác nhận. Lễ tân kiểm tra và chuyển sang Đã xác nhận; bạn theo dõi trong mục Lịch hẹn.',
  ],
  [
    'Tôi có thể đổi hoặc hủy lịch không?',
    'Có, với lịch chờ xác nhận hoặc đã xác nhận và còn trong thời hạn cho phép. Hệ thống kiểm tra quy tắc của phòng khám trước khi cập nhật.',
  ],
];
