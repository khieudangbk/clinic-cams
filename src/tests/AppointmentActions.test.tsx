// @vitest-environment jsdom
import { useState } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { api } from '../api';
import { useApp } from '../context';
import { AppointmentActionModal } from '../pages/appointments/components/AppointmentActionModal';
import { appointment, makeStore, renderPage, slot } from './fixtures';

vi.mock('../api', () => ({ api: vi.fn() }));
vi.mock('../context', () => ({ useApp: vi.fn() }));

const onClose = vi.fn();
const onUpdated = vi.fn(async () => {});
beforeEach(() => {
  vi.mocked(api).mockReset();
  vi.mocked(useApp).mockReturnValue(makeStore());
  // jsdom does not implement the browser's top-layer modal behavior.
  HTMLDialogElement.prototype.showModal = function () {
    this.setAttribute('open', '');
  };
  HTMLDialogElement.prototype.close = function () {
    this.removeAttribute('open');
  };
});

function Action({ name }: { name: string }) {
  const [busy, setBusy] = useState(false);
  const [action] = useState(() => ({ row: appointment, name }));
  return (
    <AppointmentActionModal
      action={action}
      busy={busy}
      onBusyChange={setBusy}
      onClose={onClose}
      onUpdated={onUpdated}
    />
  );
}

describe('Appointment action dialog', () => {
  it('cancels with a reason and refreshes the list', async () => {
    vi.mocked(api).mockResolvedValue({ appointment: { ...appointment, status: 'cancelled' } });
    renderPage(<Action name="cancel" />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Lý do'), 'Khong the den');
    await user.click(screen.getByRole('button', { name: 'Xác nhận' }));
    await waitFor(() => expect(onUpdated).toHaveBeenCalledOnce());
    expect(api).toHaveBeenCalledWith('/api/appointments/10/cancel', 'POST', {
      reason: 'Khong the den',
    });
    expect(onClose).toHaveBeenCalledOnce();
  });

  it('loads free slots and sends the new slot ID as a number', async () => {
    vi.mocked(api)
      .mockResolvedValueOnce({ slots: [{ ...slot, id: 99 }] })
      .mockResolvedValueOnce({ appointment });
    renderPage(<Action name="reschedule" />);
    const option = await screen.findByRole('option', { name: '09:00 – 09:30' });
    const user = userEvent.setup();
    await user.selectOptions(screen.getByRole('combobox'), option);
    await user.type(screen.getByLabelText('Lý do'), 'Doi gio');
    await user.click(screen.getByRole('button', { name: 'Xác nhận' }));
    await waitFor(() => expect(onUpdated).toHaveBeenCalledOnce());
    expect(api).toHaveBeenCalledWith(
      '/api/slots?doctor_id=2&day=2026-11-23',
      'GET',
      undefined,
      expect.any(AbortSignal),
    );
    expect(api).toHaveBeenCalledWith('/api/appointments/10/reschedule', 'POST', {
      reason: 'Doi gio',
      slot_id: 99,
    });
  });

  it('disables confirmation when no reschedule slots are available', async () => {
    vi.mocked(api).mockResolvedValue({ slots: [] });
    renderPage(<Action name="reschedule" />);
    await screen.findByText('Không có giờ trống trong ngày này.');
    expect((screen.getByRole('button', { name: 'Xác nhận' }) as HTMLButtonElement).disabled).toBe(
      true,
    );
    expect(onUpdated).not.toHaveBeenCalled();
  });

  it('keeps the dialog open when the backend rejects cancellation', async () => {
    const store = makeStore();
    vi.mocked(useApp).mockReturnValue(store);
    vi.mocked(api).mockRejectedValue(new Error('Đã quá hạn hủy lịch.'));
    renderPage(<Action name="cancel" />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Lý do'), 'Huy lich');
    await user.click(screen.getByRole('button', { name: 'Xác nhận' }));
    await waitFor(() => expect(store.notify).toHaveBeenCalledWith('Đã quá hạn hủy lịch.', true));
    expect(onClose).not.toHaveBeenCalled();
    expect(onUpdated).not.toHaveBeenCalled();
    expect((screen.getByRole('button', { name: 'Xác nhận' }) as HTMLButtonElement).disabled).toBe(
      false,
    );
  });
});
