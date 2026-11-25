// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { api } from '../api';
import { useApp } from '../context';
import { WeeklySchedule } from '../pages/staff/schedule/components/WeeklySchedule';
import { LeaveRegistration } from '../pages/staff/schedule/components/LeaveRegistration';
import { makeStore, renderPage } from './fixtures';

vi.mock('../api', () => ({ api: vi.fn() }));
vi.mock('../context', () => ({ useApp: vi.fn() }));
let store: ReturnType<typeof makeStore>;
const onSaved = vi.fn();
beforeEach(() => {
  vi.mocked(api).mockReset();
  store = makeStore();
  vi.mocked(useApp).mockReturnValue(store);
});

describe('Doctor schedule forms', () => {
  it('sends weekdays as integers and refreshes the catalog', async () => {
    vi.mocked(api).mockResolvedValue({ ok: true });
    renderPage(<WeeklySchedule current={undefined} manager doctor="2" onSaved={onSaved} />);
    const user = userEvent.setup();
    const weekdays = screen.getByLabelText('Thứ làm việc (0=Hai, 5=Bảy)');
    await user.clear(weekdays);
    await user.type(weekdays, '0,2,4');
    await user.click(screen.getByRole('button', { name: 'Lưu mẫu tuần' }));
    await waitFor(() => expect(store.refresh).toHaveBeenCalledOnce());
    expect(api).toHaveBeenCalledWith('/api/schedules', 'POST', {
      doctor_id: 2,
      weekdays: [0, 2, 4],
      start_time: '07:00',
      end_time: '17:00',
    });
    expect(onSaved).toHaveBeenCalledOnce();
  });

  it('renders the schedule without an edit form for a doctor', () => {
    renderPage(
      <WeeklySchedule
        current={{ doctor_id: 2, weekdays: '0,2', start_time: '08:00', end_time: '16:00' }}
        manager={false}
        doctor="2"
        onSaved={onSaved}
      />,
    );
    expect(screen.getByText('08:00–16:00 · Thứ 2, 4')).toBeTruthy();
    expect(screen.queryByRole('button', { name: 'Lưu mẫu tuần' })).toBeNull();
  });

  it('reports a rejected schedule and does not report success', async () => {
    vi.mocked(api).mockRejectedValue(new Error('Giờ làm việc không hợp lệ.'));
    renderPage(<WeeklySchedule current={undefined} manager doctor="2" onSaved={onSaved} />);
    await userEvent.setup().click(screen.getByRole('button', { name: 'Lưu mẫu tuần' }));
    await waitFor(() =>
      expect(store.notify).toHaveBeenCalledWith('Giờ làm việc không hợp lệ.', true),
    );
    expect(onSaved).not.toHaveBeenCalled();
  });

  it('submits offset-aware leave dates and shows affected appointments', async () => {
    vi.mocked(api).mockResolvedValue({
      id: 1,
      message: 'Đã chặn đặt mới.',
      affected: [
        {
          id: 10,
          status: 'pending',
          starts_at: '2026-11-23T10:00+07:00',
          patient_name: 'Nguyen An',
          patient_phone: '0912345678',
        },
      ],
    });
    renderPage(<LeaveRegistration doctor="2" onSaved={onSaved} />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Lý do'), 'Nghi om');
    await user.click(screen.getByRole('button', { name: 'Ghi nhận ngày nghỉ' }));
    await screen.findByText('1 lịch bị ảnh hưởng');
    expect(api).toHaveBeenCalledWith('/api/leaves', 'POST', {
      doctor_id: 2,
      starts_at: '2026-11-23T09:00:00+07:00',
      ends_at: '2026-11-23T17:00:00+07:00',
      reason: 'Nghi om',
    });
    expect(screen.getByText(/Nguyen An/).textContent).toContain('0912345678');
    expect(onSaved).toHaveBeenCalledOnce();
  });

  it('shows leave errors without displaying affected appointments', async () => {
    vi.mocked(api).mockRejectedValue(new Error('Khoảng nghỉ đã kết thúc.'));
    renderPage(<LeaveRegistration doctor="2" onSaved={onSaved} />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Lý do'), 'Nghi');
    await user.click(screen.getByRole('button', { name: 'Ghi nhận ngày nghỉ' }));
    await waitFor(() =>
      expect(store.notify).toHaveBeenCalledWith('Khoảng nghỉ đã kết thúc.', true),
    );
    expect(screen.queryByText(/lịch bị ảnh hưởng$/)).toBeNull();
    expect(onSaved).not.toHaveBeenCalled();
  });
});
