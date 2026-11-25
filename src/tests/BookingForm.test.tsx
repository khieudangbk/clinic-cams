// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { api } from '../api';
import { useApp } from '../context';
import { BookingForm } from '../pages/booking/components/BookingForm';
import { makeStore, patient, renderPage, slot } from './fixtures';

vi.mock('../api', () => ({ api: vi.fn() }));
vi.mock('../context', () => ({ useApp: vi.fn() }));

beforeEach(() => {
  vi.mocked(api).mockReset();
  vi.mocked(useApp).mockReturnValue(makeStore());
});

describe('Booking confirmation form', () => {
  it('submits a numeric slot ID and structured booking details, then opens the appointment list', async () => {
    vi.mocked(api).mockResolvedValue({ appointment: { id: 10 } });
    renderPage(<BookingForm slot={slot} user={patient} />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText(/Năm sinh/), '1990');
    await user.click(screen.getByLabelText('Nữ'));
    await user.type(screen.getByLabelText(/Lý do khám/), 'Kham tong quat');
    await user.click(screen.getByRole('button', { name: 'XÁC NHẬN ĐẶT KHÁM' }));
    await waitFor(() =>
      expect(screen.getByTestId('location').textContent).toBe('/lich-hen?created=10'),
    );
    expect(api).toHaveBeenCalledWith('/api/appointments', 'POST', {
      slot_id: 42,
      reason: 'Kham tong quat',
      booking_details: {
        booking_for: 'self',
        name: patient.name,
        phone: patient.phone,
        birth_year: '1990',
        gender: 'female',
        province: '',
        ward: '',
        address: '',
        extra_note: '',
        payment: 'at_clinic',
      },
    });
  });

  it('switches to a relative and sends their name and phone', async () => {
    vi.mocked(api).mockResolvedValue({ appointment: { id: 11 } });
    renderPage(<BookingForm slot={slot} user={patient} />);
    const user = userEvent.setup();
    await user.click(screen.getByLabelText('Đặt cho người thân'));
    await user.clear(screen.getByLabelText(/Họ tên người khám/));
    await user.type(screen.getByLabelText(/Họ tên người khám/), 'Nguoi than');
    await user.clear(screen.getByLabelText(/Số điện thoại/));
    await user.type(screen.getByLabelText(/Số điện thoại/), '0987654321');
    await user.type(screen.getByLabelText(/Lý do khám/), 'Tai kham');
    await user.click(screen.getByRole('button', { name: 'XÁC NHẬN ĐẶT KHÁM' }));
    await waitFor(() =>
      expect(api).toHaveBeenCalledWith(
        '/api/appointments',
        'POST',
        expect.objectContaining({
          booking_details: expect.objectContaining({
            booking_for: 'relative',
            name: 'Nguoi than',
            phone: '0987654321',
          }),
        }),
      ),
    );
  });

  it('shows a slot conflict and re-enables submission', async () => {
    vi.mocked(api).mockRejectedValue(new Error('Khung giờ đã có người đặt.'));
    renderPage(<BookingForm slot={slot} user={patient} />, '/dat-lich-kham/2');
    const user = userEvent.setup();
    await user.type(screen.getByLabelText(/Lý do khám/), 'Kham');
    await user.click(screen.getByRole('button', { name: 'XÁC NHẬN ĐẶT KHÁM' }));
    expect((await screen.findByRole('alert')).textContent).toContain('Khung giờ đã có người đặt.');
    expect(
      (screen.getByRole('button', { name: 'XÁC NHẬN ĐẶT KHÁM' }) as HTMLButtonElement).disabled,
    ).toBe(false);
    expect(screen.getByTestId('location').textContent).toBe('/dat-lich-kham/2');
  });

  it('blocks duplicate booking requests while waiting for confirmation', async () => {
    let complete!: (response: { appointment: { id: number } }) => void;
    vi.mocked(api).mockReturnValue(
      new Promise((resolve) => {
        complete = resolve;
      }),
    );
    renderPage(<BookingForm slot={slot} user={patient} />);
    const user = userEvent.setup();
    await user.type(screen.getByLabelText(/Lý do khám/), 'Kham');
    await user.click(screen.getByRole('button', { name: 'XÁC NHẬN ĐẶT KHÁM' }));
    const busyButton = screen.getByRole('button', { name: 'ĐANG GỬI…' }) as HTMLButtonElement;
    expect(busyButton.disabled).toBe(true);
    await user.click(busyButton);
    expect(api).toHaveBeenCalledOnce();
    await act(async () => {
      complete({ appointment: { id: 10 } });
    });
    await waitFor(() =>
      expect(screen.getByTestId('location').textContent).toBe('/lich-hen?created=10'),
    );
  });

  it('does not submit an incomplete required form, including a null phone', async () => {
    renderPage(<BookingForm slot={slot} user={{ ...patient, phone: null }} />);
    const user = userEvent.setup();
    expect((screen.getByLabelText(/Số điện thoại/) as HTMLInputElement).value).toBe('');
    await user.click(screen.getByRole('button', { name: 'XÁC NHẬN ĐẶT KHÁM' }));
    expect(api).not.toHaveBeenCalled();
  });
});
