import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api, setCsrf } from '../api';
import { useApp } from '../context';
import { Breadcrumb, ErrorBox } from '../components/UI';
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
