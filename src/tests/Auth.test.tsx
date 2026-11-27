// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { api, setCsrf } from '../api';
import { useApp } from '../context';
import { Auth } from '../pages/account/Auth';
import { makeStore, patient, renderPage } from './fixtures';

vi.mock('../api', () => ({ api: vi.fn(), setCsrf: vi.fn() }));
vi.mock('../context', () => ({ useApp: vi.fn() }));

let store: ReturnType<typeof makeStore>;
beforeEach(() => {
  vi.mocked(api).mockReset();
  store = makeStore({ user: null });
  vi.mocked(useApp).mockReturnValue(store);
});

async function fillLogin() {
  const user = userEvent.setup();
  await user.type(screen.getByLabelText('Email'), patient.email);
  await user.type(screen.getByLabelText('Mật khẩu'), 'Testing@12345');
  return user;
}

describe('Account forms', () => {
  it('logs a patient in, stores CSRF and returns to the requested page', async () => {
    vi.mocked(api).mockResolvedValue({ user: patient, csrf: 'csrf-123' });
    renderPage(<Auth />, '/dang-nhap?next=%2Fdat-lich-kham%2F2%3Fslot%3D42');
    const user = await fillLogin();
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));
    await waitFor(() =>
      expect(screen.getByTestId('location').textContent).toBe('/dat-lich-kham/2?slot=42'),
    );
    expect(api).toHaveBeenCalledWith('/api/login', 'POST', {
      email: patient.email,
      password: 'Testing@12345',
    });
    expect(setCsrf).toHaveBeenCalledWith('csrf-123');
    expect(store.refresh).toHaveBeenCalledOnce();
  });

  it('routes staff to their workspace', async () => {
    vi.mocked(api).mockResolvedValue({
      user: { ...patient, role: 'receptionist' },
      csrf: 'staff-csrf',
    });
    renderPage(<Auth />);
    const user = await fillLogin();
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));
    await waitFor(() => expect(screen.getByTestId('location').textContent).toBe('/nhan-vien'));
  });

  it('rejects an external next URL', async () => {
    vi.mocked(api).mockResolvedValue({ user: patient, csrf: 'csrf' });
    renderPage(<Auth />, '/dang-nhap?next=https%3A%2F%2Fexample.com');
    const user = await fillLogin();
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));
    await waitFor(() => expect(screen.getByTestId('location').textContent).toBe('/'));
  });

  it('shows invalid credentials and lets the user retry without navigating', async () => {
    vi.mocked(api).mockRejectedValue(new Error('Sai email hoặc mật khẩu.'));
    renderPage(<Auth />, '/dang-nhap');
    const user = await fillLogin();
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));
    expect((await screen.findByRole('alert')).textContent).toContain('Sai email hoặc mật khẩu.');
    expect(screen.getByTestId('location').textContent).toBe('/dang-nhap');
    expect((screen.getByRole('button', { name: 'Đăng nhập' }) as HTMLButtonElement).disabled).toBe(
      false,
    );
    expect(setCsrf).not.toHaveBeenCalled();
    expect(store.refresh).not.toHaveBeenCalled();
  });

  it('disables login while the request is pending to prevent duplicate submission', async () => {
    let complete!: (response: { user: typeof patient; csrf: string }) => void;
    vi.mocked(api).mockReturnValue(
      new Promise((resolve) => {
        complete = resolve;
      }),
    );
    renderPage(<Auth />);
    const user = await fillLogin();
    await user.click(screen.getByRole('button', { name: 'Đăng nhập' }));
    const busyButton = screen.getByRole('button', { name: 'Đang xử lý…' }) as HTMLButtonElement;
    expect(busyButton.disabled).toBe(true);
    await user.click(busyButton);
    expect(api).toHaveBeenCalledOnce();
    await act(async () => {
      complete({ user: patient, csrf: 'csrf' });
    });
    await waitFor(() => expect(screen.getByTestId('location').textContent).toBe('/'));
  });

  it('registers a patient with a string phone and returns to the login form', async () => {
    vi.mocked(api).mockResolvedValue({ id: 7, message: 'Created' });
    renderPage(<Auth />);
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Chưa có tài khoản? Đăng ký' }));
    await user.type(screen.getByLabelText('Họ và tên'), patient.name);
    await user.type(screen.getByLabelText('Số điện thoại'), patient.phone!);
    await user.type(screen.getByLabelText('Email'), patient.email);
    await user.type(screen.getByLabelText('Mật khẩu'), 'Testing@12345');
    await user.click(screen.getByRole('button', { name: 'Đăng ký' }));
    await screen.findByRole('heading', { name: 'Đăng nhập CAMS' });
    expect(api).toHaveBeenCalledWith('/api/register', 'POST', {
      name: patient.name,
      phone: patient.phone,
      email: patient.email,
      password: 'Testing@12345',
    });
    expect(setCsrf).not.toHaveBeenCalled();
  });
});
