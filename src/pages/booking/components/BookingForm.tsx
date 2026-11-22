import type { CreateAppointmentRequest } from '../../../interfaces/appointment/CreateAppointmentRequest';
import type { AppointmentResponse } from '../../../interfaces/appointment/AppointmentResponse';
import type { BookingDetails } from '../../../interfaces/appointment/BookingDetails';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserRound, Phone, MapPin } from 'lucide-react';
import { api } from '../../../api';
import { useApp } from '../../../context';
import { ErrorBox } from '../../../components/UI';
import type { BookingFormProps } from '../../../interfaces/pages/BookingFormProps';

export function BookingForm({ slot, user }: BookingFormProps) {
  const { catalog, notify } = useApp();
  const nav = useNavigate();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [who, setWho] = useState<'self' | 'relative'>('self');
  return (
    <form
      onSubmit={async (e) => {
        e.preventDefault();
        setBusy(true);
        setError('');
        const data = new FormData(e.currentTarget);
        const gender = data.get('gender');
        const booking_details: BookingDetails = {
          booking_for: who,
          name: String(data.get('name') ?? ''),
          phone: String(data.get('phone') ?? ''),
          birth_year: String(data.get('birth_year') ?? ''),
          gender: gender === 'male' || gender === 'female' || gender === 'other' ? gender : '',
          province: String(data.get('province') ?? ''),
          ward: String(data.get('ward') ?? ''),
          address: String(data.get('address') ?? ''),
          extra_note: String(data.get('extra_note') ?? ''),
          payment: 'at_clinic',
        };
        try {
          const r = await api<AppointmentResponse, CreateAppointmentRequest>(
            '/api/appointments',
            'POST',
            {
              slot_id: slot!.id,
              reason: String(data.get('reason') ?? ''),
              booking_details,
            },
          );
          notify(`Đã tạo lịch #${r.appointment.id}. Đang chờ xác nhận.`);
          nav('/lich-hen?created=' + r.appointment.id);
        } catch (err) {
          setError((err as Error).message);
          setBusy(false);
        }
      }}
    >
      <fieldset className="radio-group">
        <legend>Đặt khám cho</legend>
        {[
          ['self', 'Đặt cho mình'],
          ['relative', 'Đặt cho người thân'],
        ].map(([v, t]) => (
          <label key={v}>
            <input
              type="radio"
              name="booking_for"
              value={v}
              checked={who === v}
              onChange={() => setWho(v === 'relative' ? 'relative' : 'self')}
            />
            {t}
          </label>
        ))}
      </fieldset>
      <div className="form-grid" key={who}>
        <label>
          Họ tên người khám *
          <div className="input-with-icon">
            <UserRound size={17} />
            <input
              name="name"
              required
              maxLength={100}
              autoComplete="name"
              defaultValue={who === 'self' ? user.name : ''}
              placeholder="Họ và tên"
            />
          </div>
        </label>
        <label>
          Số điện thoại *
          <div className="input-with-icon">
            <Phone size={17} />
            <input
              name="phone"
              type="tel"
              required
              pattern="[+0-9 ()-]{8,20}"
              defaultValue={who === 'self' ? (user.phone ?? '') : ''}
              placeholder="Số điện thoại"
            />
          </div>
        </label>
        <label>
          Năm sinh
          <input
            name="birth_year"
            type="number"
            min={1900}
            max={new Date(catalog!.now).getFullYear()}
            placeholder="Năm sinh"
          />
        </label>
        <fieldset className="radio-group gender">
          <legend>Giới tính</legend>
          <label>
            <input type="radio" name="gender" value="male" />
            Nam
          </label>
          <label>
            <input type="radio" name="gender" value="female" />
            Nữ
          </label>
          <label>
            <input type="radio" name="gender" value="other" />
            Khác
          </label>
        </fieldset>
        <label>
          Tỉnh / thành phố
          <input name="province" placeholder="Tỉnh / thành phố" maxLength={100} />
        </label>
        <label>
          Phường / xã
          <input name="ward" placeholder="Phường / xã" maxLength={100} />
        </label>
      </div>
      <label>
        Địa chỉ chi tiết
        <div className="input-with-icon">
          <MapPin size={17} />
          <input name="address" maxLength={500} placeholder="Số nhà, đường…" />
        </div>
      </label>
      <label>
        Lý do khám *
        <textarea
          name="reason"
          required
          maxLength={500}
          placeholder="Mô tả ngắn gọn lý do cần khám"
        />
      </label>
      <label>
        Ghi chú thêm
        <textarea
          name="extra_note"
          maxLength={500}
          placeholder="Thông tin cần trao đổi với phòng khám (nếu có)"
        />
      </label>
      <fieldset className="radio-group">
        <legend>Hình thức thanh toán</legend>
        <label>
          <input type="radio" name="payment" value="at_clinic" defaultChecked />
          Thanh toán sau tại cơ sở y tế
        </label>
      </fieldset>
      <p className="form-helper">
        Yêu cầu trực tuyến cần được xác nhận. Bản demo không thu tiền và không dùng thông tin thật.
      </p>
      {error && <ErrorBox message={error} />}
      <button className="booking-cta" disabled={busy}>
        {busy ? 'ĐANG GỬI…' : 'XÁC NHẬN ĐẶT KHÁM'}
      </button>
      <p className="terms-line">
        Bằng việc đặt lịch, bạn xác nhận thông tin đã nhập và đã đọc{' '}
        <Link to="/quyen-rieng-tu">thông tin quyền riêng tư</Link>.
      </p>
    </form>
  );
}
