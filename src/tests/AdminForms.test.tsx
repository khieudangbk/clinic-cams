// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useApp } from '../context';
import { CreateStaffForm } from '../pages/staff/admin/components/CreateStaffForm';
import { StaffAccounts } from '../pages/staff/admin/components/StaffAccounts';
import { BookingSettingsForm } from '../pages/staff/admin/components/BookingSettingsForm';
import type { AdminSave } from '../interfaces/admin/AdminSave';
import { makeStore, patient, renderPage } from './fixtures';

vi.mock('../context', () => ({ useApp: vi.fn() }));
const save = vi.fn<AdminSave>();
const doctor = { ...patient, id: 2, role: 'doctor' as const, specialty_id: 1 };
beforeEach(() => {
  save.mockReset().mockResolvedValue(true);
  vi.mocked(useApp).mockReturnValue(makeStore());
});

describe('Administration forms', () => {
  it('creates a doctor with a numeric specialty and resets after success', async () => {
    renderPage(<CreateStaffForm save={save} />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Họ tên'), 'BS Moi');
    await user.type(screen.getByLabelText('Email'), 'doctor@example.com');
    await user.type(screen.getByLabelText('Mật khẩu ban đầu'), 'Testing@12345');
    await user.selectOptions(screen.getByLabelText('Chuyên khoa'), '2');
    await user.click(screen.getByRole('button', { name: 'Tạo tài khoản' }));
    await waitFor(() =>
      expect(save).toHaveBeenCalledWith('/api/users', {
        name: 'BS Moi',
        email: 'doctor@example.com',
        password: 'Testing@12345',
        role: 'doctor',
        specialty_id: 2,
      }),
    );
    await waitFor(() =>
      expect((screen.getByLabelText('Họ tên') as HTMLInputElement).value).toBe(''),
    );
  });

  it('preserves form values when saving fails', async () => {
    save.mockResolvedValue(false);
    renderPage(<CreateStaffForm save={save} />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Họ tên'), 'BS Moi');
    await user.type(screen.getByLabelText('Email'), 'doctor@example.com');
    await user.type(screen.getByLabelText('Mật khẩu ban đầu'), 'Testing@12345');
    await user.click(screen.getByRole('button', { name: 'Tạo tài khoản' }));
    await waitFor(() => expect(save).toHaveBeenCalledOnce());
    expect((screen.getByLabelText('Họ tên') as HTMLInputElement).value).toBe('BS Moi');
  });

  it.each([
    [1, 'Khóa', false],
    [0, 'Mở khóa', true],
  ] as const)(
    'converts active flag %i to a boolean on update',
    async (active, button, expected) => {
      renderPage(<StaffAccounts users={[{ ...doctor, active }]} save={save} />);
      const user = userEvent.setup();
      await user.click(screen.getByRole('button', { name: button }));
      expect(save).toHaveBeenCalledWith('/api/users/2', { active: expected });
    },
  );

  it('edits a doctor using only the changed fields', async () => {
    renderPage(<StaffAccounts users={[doctor]} save={save} />);
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Sửa' }));
    await user.clear(screen.getByLabelText('Họ tên'));
    await user.type(screen.getByLabelText('Họ tên'), 'BS Da Sua');
    await user.selectOptions(screen.getByLabelText('Chuyên khoa'), '2');
    await user.click(screen.getByRole('button', { name: 'Lưu' }));
    await waitFor(() =>
      expect(save).toHaveBeenCalledWith('/api/users/2', { name: 'BS Da Sua', specialty_id: 2 }),
    );
    await waitFor(() => expect(screen.queryByRole('button', { name: 'Lưu' })).toBeNull());
  });

  it('does not show editing or locking controls for managers', () => {
    renderPage(<StaffAccounts users={[{ ...patient, role: 'manager' }]} save={save} />);
    expect(screen.queryByRole('button', { name: 'Sửa' })).toBeNull();
    expect(screen.queryByRole('button', { name: 'Khóa' })).toBeNull();
  });

  it('sends all booking settings as numbers', async () => {
    renderPage(<BookingSettingsForm save={save} />);
    const user = userEvent.setup();
    const minutes = screen.getByLabelText('Độ dài slot mới (phút)');
    await user.clear(minutes);
    await user.type(minutes, '45');
    await user.click(screen.getByRole('button', { name: 'Lưu quy tắc' }));
    expect(save).toHaveBeenCalledWith('/api/settings', {
      slot_minutes: 45,
      cutoff_hours: 2,
      max_reschedules: 2,
    });
  });
});
