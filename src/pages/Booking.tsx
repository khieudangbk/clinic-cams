import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { api, setCsrf } from '../api';
import { useApp } from '../context';
import { dateText, type Slot } from '../types';
import { media, doctorImage } from '../data';
import { Breadcrumb, ErrorBox, Loading, Empty } from '../components/UI';
export function Auth() {
  const { user, refresh, notify } = useApp();
  const nav = useNavigate();
  const [register, setRegister] = useState(false),
    [busy, setBusy] = useState(false),
    [error, setError] = useState('');
  const [params] = useSearchParams();
  const raw = params.get('next') || '/';
  const next = raw.startsWith('/') && !raw.startsWith('//') && !raw.includes('\\') ? raw : '/';
  const staff = next.startsWith('/nhan-vien');
  if (user)
    return (
      <div className="container page auth-card">
        <h1>Bạn đã đăng nhập</h1>
        <p>{user.name}</p>
        <Link className="primary" to={user.role === 'patient' ? next : '/nhan-vien'}>
          Tiếp tục
        </Link>
      </div>
    );
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: register ? 'Đăng ký' : 'Đăng nhập' }]} />
      <div className="auth-card">
        <h1>
          {register ? 'Tạo tài khoản bệnh nhân' : staff ? 'Đăng nhập nhân viên' : 'Đăng nhập CAMS'}
        </h1>
        <p>
          {next.startsWith('/dat-lich')
            ? 'Đăng nhập để xác nhận khung giờ bạn đã chọn. Giờ khám chỉ được giữ khi đặt lịch thành công.'
            : 'Tìm kiếm và xem lịch công khai; dùng tài khoản để quản lý lịch hẹn.'}
        </p>
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setBusy(true);
            setError('');
            const data = Object.fromEntries(new FormData(e.currentTarget));
            try {
              if (register) {
                await api('/api/register', 'POST', data);
                setRegister(false);
                notify('Đăng ký thành công. Vui lòng đăng nhập.');
              } else {
                const r = await api('/api/login', 'POST', data);
                setCsrf(r.csrf);
                await refresh();
                nav(r.user.role === 'patient' ? next : '/nhan-vien', { replace: true });
              }
            } catch (err) {
              setError((err as Error).message);
            } finally {
              setBusy(false);
            }
          }}
        >
          {register && (
            <>
              <label>
                Họ và tên
                <input name="name" autoComplete="name" maxLength={100} required />
              </label>
              <label>
                Số điện thoại
                <input
                  name="phone"
                  type="tel"
                  autoComplete="tel"
                  pattern="[+0-9 ()-]{8,20}"
                  required
                />
              </label>
            </>
          )}
          <label>
            Email
            <input name="email" type="email" autoComplete="username" maxLength={150} required />
          </label>
          <label>
            Mật khẩu
            <input
              name="password"
              type="password"
              autoComplete={register ? 'new-password' : 'current-password'}
              minLength={10}
              maxLength={128}
              required
            />
          </label>
          {error && <ErrorBox message={error} />}
          <button className="primary full" disabled={busy}>
            {busy ? 'Đang xử lý…' : register ? 'Đăng ký' : 'Đăng nhập'}
          </button>
        </form>
        {!staff && (
          <button
            className="text-link"
            onClick={() => {
              setRegister(!register);
              setError('');
            }}
          >
            {register ? 'Đã có tài khoản? Đăng nhập' : 'Chưa có tài khoản? Đăng ký'}
          </button>
        )}
        <details className="demo-accounts">
          <summary>Tài khoản trải nghiệm</summary>
          <p>
            Chỉ dùng thông tin giả lập. Mật khẩu: <code>Demo@12345</code>
          </p>
          <ul>
            {(staff ? ['admin', 'staff', 'doctor01'] : ['patient']).map((s) => (
              <li key={s}>{s}@cams.local</li>
            ))}
          </ul>
        </details>
        <Link to="/">← Về trang chủ</Link>
      </div>
    </div>
  );
}
export function Booking() {
  const { id } = useParams();
  const [params] = useSearchParams();
  const { catalog, user } = useApp();
  const [slot, setSlot] = useState<Slot | null>(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState('');
  const location = useLocation();
  const d = catalog!.doctors.find((x) => x.id === Number(id)),
    day = params.get('day') || catalog!.now.slice(0, 10),
    slotId = Number(params.get('slot'));
  useEffect(() => {
    const c = new AbortController();
    setLoading(true);
    setError('');
    api<{ slots: Slot[] }>(
      `/api/public/slots?doctor_id=${Number(id)}&day=${encodeURIComponent(day)}`,
      'GET',
      undefined,
      c.signal,
    )
      .then((r) => {
        const value = r.slots.find((x) => x.id === slotId);
        if (!value)
          throw new Error('Khung giờ đã được đặt hoặc không còn khả dụng. Vui lòng chọn giờ khác.');
        setSlot(value);
      })
      .catch((e) => {
        if (e.name !== 'AbortError') setError(e.message);
      })
      .finally(() => {
        if (!c.signal.aborted) setLoading(false);
      });
    return () => c.abort();
  }, [id, day, slotId]);
  if (!d)
    return (
      <Empty title="Không tìm thấy bác sĩ">
        <Link to="/bac-si">Tìm bác sĩ khác</Link>
      </Empty>
    );
  if (loading) return <Loading />;
  if (error && !slot)
    return (
      <div className="container page">
        <ErrorBox message={error} />
        <Link to={'/bac-si/' + id}>Chọn khung giờ khác →</Link>
      </div>
    );
  if (!user)
    return (
      <div className="container page booking-form">
        <div className="booking-summary">
          <img src={media + doctorImage(d)} alt="" />
          <div>
            <p>Đặt lịch khám</p>
            <h1>{d.name}</h1>
            <span>
              {dateText(slot!.starts_at)} — {slot!.ends_at.slice(11, 16)}
            </span>
            <p>Phòng khám CAMS</p>
          </div>
        </div>
        <p>Đăng nhập để hoàn tất yêu cầu. Lựa chọn giờ khám sẽ được giữ trong đường dẫn này.</p>
        <Link
          className="primary full"
          to={'/dang-nhap?next=' + encodeURIComponent(location.pathname + location.search)}
        >
          Đăng nhập để tiếp tục
        </Link>
        <Link to={'/bac-si/' + id}>Chọn lại giờ khám</Link>
      </div>
    );
  if (user.role !== 'patient')
    return (
      <div className="container page">
        <h1>Đặt hộ bệnh nhân</h1>
        <p>Vui lòng dùng chức năng đặt lịch trong khu vực lễ tân.</p>
        <Link to="/nhan-vien" className="primary">
          Mở khu vực nhân viên
        </Link>
      </div>
    );
  return <div className="container page"><h1>{d.name}</h1><p>{dateText(slot!.starts_at)}</p><Link to={'/bac-si/' + id}>Chọn lại giờ khám</Link></div>;
}
