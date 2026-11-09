# CAMS Backend — Clinic Appointment Management System

Backend Spring Boot cho hệ thống đặt lịch khám CAMS: bệnh nhân đặt lịch, lễ tân duyệt/đặt hộ, bác sĩ đăng ký nghỉ, quản lý cấu hình nhân sự và lịch làm việc. Frontend React nằm ở `../frontend`.

- Java 21, Spring Boot 4.1, Spring Data JPA, Spring Security
- PostgreSQL 16, schema và dữ liệu mẫu do Flyway quản lý
- Tài liệu API: Swagger UI (springdoc-openapi)
- Test: JUnit 5, MockMvc, H2

## 1. Yêu cầu

| Công cụ | Phiên bản |
|---|---|
| JDK | 21 |
| Maven | dùng `./mvnw` đi kèm, không cần cài |
| Docker | để chạy PostgreSQL |
| Node.js | chỉ khi chạy frontend |

## 2. Chạy nhanh

```bash
cd backend
docker compose up -d          # PostgreSQL ở localhost:5437
./mvnw spring-boot:run        # backend ở http://localhost:8080
```

Terminal khác, chạy frontend:

```bash
cd frontend
npm ci && npm run dev         # http://127.0.0.1:5173, proxy /api → 127.0.0.1:8080
```

Lần chạy đầu trên DB rỗng, Flyway tự tạo bảng và seed dữ liệu mẫu. Nếu gặp lỗi migration do file cũ trong `target/`, chạy `./mvnw clean spring-boot:run`.

| Dịch vụ | Địa chỉ |
|---|---|
| Backend | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Frontend | http://127.0.0.1:5173 |
| PostgreSQL | localhost:5437, DB `cams`, user `postgres` |

### Tài khoản demo

Mật khẩu chung: **`Demo@12345`**

| Vai trò | Email |
|---|---|
| Quản lý (`manager`) | `admin@cams.local` |
| Lễ tân (`receptionist`) | `staff@cams.local` |
| Bệnh nhân (`patient`) | `patient@cams.local` |
| Bác sĩ (`doctor`) | `doctor01@cams.local` … `doctor10@cams.local` |

## 3. Cấu hình (`.env`)

Backend đọc `.env` ở thư mục `backend/` (`EnvLoader` và `spring.config.import`). Các biến chính:

| Biến | Ý nghĩa | Ví dụ |
|---|---|---|
| `DB_URL` | JDBC URL PostgreSQL, phải khớp `docker-compose.yml` | `jdbc:postgresql://localhost:5437/cams?sslmode=disable` |
| `DB_USERNAME` / `DB_PASSWORD` | Tài khoản DB, khớp `POSTGRES_USER` / `POSTGRES_PASSWORD` trong `docker-compose.yml` | `postgres` |
| `SERVER_PORT` | Cổng backend, khớp proxy trong `frontend/vite.config.ts` | `8080` |
| `CORS_ALLOWED_ORIGINS` | Các frontend được phép, ngăn cách bằng dấu phẩy. **Bắt buộc**, thiếu thì backend không khởi động | `http://localhost:3000,http://localhost:5173` |
| `CAMS_DEMO_NOW` | Đồng hồ giả lập (ISO, có offset +07:00). Để trống để dùng giờ thật | `2026-11-23T08:00:00+07:00` |

Không commit `.env` hay mật khẩu thật lên Git.

## 4. Kiến trúc

API đặt lịch nằm dưới `/api/**` và dùng tài khoản CAMS trong bảng `cams_users`.

### Xác thực CAMS

- `POST /api/login` đặt cookie HttpOnly `cams_session` (SameSite=Strict, hiệu lực 8 giờ) và trả `csrf`.
- Các POST yêu cầu đăng nhập phải gửi header `X-CSRF-Token` bằng giá trị `csrf`.
- Host chỉ được là `localhost` hoặc `127.0.0.1`; POST có header `Origin` thì phải cùng nguồn (`CamsOriginGuard`).
- Đăng nhập sai 5 lần liên tiếp thì khóa email đó 60 giây (`CamsLoginRateLimiter`).
- Xác thực và CSRF được kiểm tra trong từng controller qua `CamsAuthContext`. Spring Security chỉ cho các route CAMS đi qua (`camsFilterChain` là `permitAll`).
- Thành công trả JSON thô; lỗi trả `{"error": "...", "code": "..."}` (`CamsExceptionHandler`).

### Vai trò

| Vai trò | Quyền chính |
|---|---|
| `patient` | Đặt lịch cho mình, đổi/hủy trước giờ hẹn, xem lịch của mình |
| `receptionist` | Đặt hộ khách, duyệt/từ chối, check-in, hoàn tất, đánh dấu vắng, đổi/hủy |
| `doctor` | Xem lịch của mình, đăng ký nghỉ |
| `manager` | Quản lý nhân sự, chuyên khoa, cấu hình, mẫu giờ làm việc, xem nghỉ của mọi bác sĩ |

### Vòng đời lịch hẹn

```
pending ──approve──▶ confirmed ──checkin──▶ arrived ──complete──▶ completed
   │                     │
   ├─reject─▶ cancelled  └─noshow─▶ noshow
   └─cancel─▶ cancelled        (cancel/reschedule cũng áp dụng cho confirmed)
```

Bệnh nhân đặt online thì vào `pending`; lễ tân đặt hộ thì vào `confirmed`. Mỗi khung giờ chỉ có tối đa một lịch đang hoạt động (`pending`, `confirmed`, `arrived`).

### Cấu trúc thư mục

```
src/main/java/com/example/cams/
├── config/        SecurityConfiguration, JacksonConfig, CorsConfig, OpenApiConfig, CamsDataSeeder
├── controller/    Cams*Controller (API đặt lịch)
├── service/       Cams*Service (nghiệp vụ)
├── repository/    Spring Data JPA
├── entity/        Cams* (bảng cams_*) và enums
├── security/      CamsAuthContext, CamsOriginGuard, CamsLoginRateLimiter
├── dto/           request/ và response/, chia theo nghiệp vụ
└── util/          CamsTime, CamsClock, CamsValidation, exception
src/main/resources/
├── application.yml
└── db/migration/  V1__init_auth.sql, V2__cams_booking.sql, V3__cams_seed_data.sql
```

## 5. Cơ sở dữ liệu

Schema do **Flyway** quản lý; Hibernate chỉ `validate`. Không dùng `ddl-auto=update`.

| Migration | Nội dung |
|---|---|
| `V1__init_auth.sql` | Migration lịch sử tạo bảng `users`, hiện luồng CAMS dùng `cams_users` |
| `V2__cams_booking.sql` | Bảng `cams_*` và cấu hình mặc định (`slot_minutes`, `cutoff_hours`, `max_reschedules`, `advance_days`, `max_active`) |
| `V3__cams_seed_data.sql` | 3 chuyên khoa, tài khoản demo, 10 bác sĩ và mẫu giờ làm việc (có thể chạy lại an toàn) |

Các bảng chính: `cams_users`, `cams_specialties`, `cams_schedules`, `cams_slots`, `cams_appointments`, `cams_audit`, `cams_leaves`, `cams_settings`, `cams_sessions`.

Khung giờ (`cams_slots`) được sinh theo ngày khi có người xem. Mỗi bác sĩ có một mẫu tuần (ngày làm việc 0–5 = Thứ Hai–Thứ Bảy, giờ 07:00–17:00).

Muốn làm lại DB từ đầu:

```bash
docker compose down -v && docker compose up -d
```

## 6. Danh sách API

Mô tả chi tiết từng endpoint, payload và mã lỗi xem ở Swagger UI. Tóm tắt:

| Nhóm | Endpoint |
|---|---|
| Public | `GET /api/health`, `GET /api/public/catalog`, `GET /api/public/slots`, `POST /api/register`, `POST /api/login` |
| Session | `GET /api/session`, `POST /api/logout` |
| Catalog | `GET /api/catalog`, `GET /api/slots` |
| Appointments | `GET`/`POST /api/appointments`, `GET /api/appointments/{id}/audit`, `POST /api/appointments/{id}/{approve\|reject\|cancel\|reschedule\|checkin\|complete\|noshow}` |
| Leaves | `GET`/`POST /api/leaves` |
| Schedules | `POST /api/schedules` |
| Admin | `GET`/`POST /api/users`, `POST /api/users/{id}`, `POST /api/specialties`, `POST /api/settings` |

Ví dụ nhanh bằng curl:

```bash
curl -i -c jar.txt -H 'Content-Type: application/json' -H 'Host: 127.0.0.1' \
  -d '{"email":"patient@cams.local","password":"Demo@12345"}' \
  http://127.0.0.1:8080/api/login
# lấy "csrf" trong response rồi gửi kèm các request ghi:
curl -b jar.txt -H 'Content-Type: application/json' -H 'X-CSRF-Token: <csrf>' \
  -d '{"slot_id":101,"reason":"Khám tổng quát"}' \
  http://127.0.0.1:8080/api/appointments
```

## 7. Test

```bash
./mvnw test
```

Test dùng H2 (`src/test/resources/application.yml`), tắt Flyway và để Hibernate tạo schema, vì migration dùng cú pháp riêng của PostgreSQL (partial unique index). Dữ liệu demo trong test do `CamsDataSeeder` tạo.

| Loại | Lớp |
|---|---|
| Unit | `CamsValidationTest`, `CamsTimeTest`, `CamsLoginRateLimiterTest`, `CamsOriginGuardTest`, `CamsAuthContextTest` |
| Integration | `CamsApiIntegrationTest` (xác thực, CSRF, Host/Origin, rate limit, đặt trùng slot, phân quyền), `CamsBookingFlowTest` (luồng bệnh nhân, lễ tân, quản lý), `OpenApiDocsTest` (Swagger) |

## 8. Lưu ý

- Swagger UI và `/v3/api-docs` đang mở công khai. Khi deploy thật có thể tắt bằng `springdoc.api-docs.enabled=false`.
- Dữ liệu giả thêm thủ công vào DB (nếu có) nằm trong volume `postgres_data`, sẽ mất khi `docker compose down -v`.
- `CamsDataSeeder` vẫn còn để test H2 có dữ liệu; trên PostgreSQL nó không làm gì vì V3 đã seed.
