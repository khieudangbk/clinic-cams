# CAMS Frontend

Ứng dụng React, TypeScript và Vite cho hệ thống quản lý lịch hẹn phòng khám CAMS. Bệnh nhân tra cứu bác sĩ, chọn khung giờ và quản lý lịch của mình. Lễ tân, bác sĩ và quản lý dùng khu vực `/nhan-vien` theo quyền tài khoản.

## Chạy cục bộ

Cần Node.js 22.12 trở lên, JDK 21 và Docker. Chạy backend Spring Boot với PostgreSQL trước khi mở frontend.

**Terminal 1 — backend:** Từ thư mục `backend/`, nếu chưa có cấu hình cục bộ, tạo `.env` từ `.env.example` và `docker-compose.yml` từ `docker-compose.yml.example`. Chỉnh thông số PostgreSQL trong hai tệp cho khớp nhau. Xem [hướng dẫn backend](../backend/README.md) để biết các biến cần thiết.

Trên Windows PowerShell:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Trên Linux/macOS, dùng `./mvnw spring-boot:run` thay cho lệnh Maven Wrapper của Windows. Kiểm tra backend tại <http://127.0.0.1:8080/api/health>.

**Terminal 2 — frontend:** Từ thư mục `frontend/`, chạy:

```sh
npm ci
npm run dev
```

Mở <http://127.0.0.1:5173>. Vite chuyển request `/api` đến `http://127.0.0.1:8080`. Nếu không tải được danh mục, kiểm tra backend đã chạy và cổng proxy trong `vite.config.ts` khớp với `SERVER_PORT` của backend.

Muốn chạy demo với ngày hẹn cố định, đặt `CAMS_DEMO_NOW=2026-11-23T08:00:00+07:00` trong `.env` của backend rồi khởi động lại backend. Bỏ biến này để dùng giờ thực.

## Tài khoản demo

Dùng các tài khoản dữ liệu mẫu với mật khẩu chung `Demo@12345`:

| Vai trò | Email |
| --- | --- |
| Bệnh nhân | `patient@cams.local` |
| Lễ tân | `staff@cams.local` |
| Bác sĩ | `doctor01@cams.local` đến `doctor10@cams.local` |
| Quản lý | `admin@cams.local` |

Khách có thể xem chuyên khoa, bác sĩ và giờ trống. Bệnh nhân đăng nhập ở `/dang-nhap` để đặt lịch và xem `/lich-hen`. Nhân viên vào `/nhan-vien` để xử lý lịch, quản lý lịch bác sĩ hoặc xem dashboard theo quyền.

## Tổ chức mã nguồn

| Vị trí | Nội dung |
| --- | --- |
| `src/App.tsx`, `src/main.tsx` | Khởi động ứng dụng và khai báo route. |
| `src/context.tsx`, `src/api.ts` | Nạp danh mục và phiên, gọi API bằng cookie và gửi CSRF cho thao tác ghi. |
| `src/pages/`, `src/components/` | Trang công khai, luồng đặt khám, lịch cá nhân và khu vực nhân viên. |
| `src/data.ts`, `public/media/` | Nội dung tĩnh, ảnh và font cục bộ. Nguồn tài nguyên nằm trong `public/media/sources.json`. |
| `src/styles.css` | Kiểu giao diện chung và bố cục theo kích thước màn hình. |

Danh mục, khung giờ và lịch hẹn lấy từ backend. Banner và cẩm nang là nội dung tĩnh. Việc chọn giờ trên giao diện chưa giữ chỗ; backend kiểm tra slot khi nhận yêu cầu đặt lịch.

## Lệnh kiểm tra và build

```sh
npm run check
npm run build
npm run preview
```

`npm run build` xuất bản tĩnh vào `dist/`. Preview chạy ở <http://127.0.0.1:4173> và vẫn cần backend ở cổng 8080. Nếu triển khai `dist/` trên máy chủ riêng, cấu hình đường dẫn `/api` về Spring Boot và fallback các route giao diện về `index.html`.
