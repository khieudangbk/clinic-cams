import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { CalendarDays, MousePointerClick } from 'lucide-react';
import { api } from '../api';
import { useApp } from '../context';
import { dayOffset, type Doctor, type Slot } from '../types';
import { Loading, ErrorBox } from './UI';
export function AddressBlock() {
  return (
    <aside className="address-block">
      <strong>ĐỊA CHỈ KHÁM</strong>
      <Link to="/co-so-y-te/cams">Phòng khám CAMS</Link>
      <p>
        Cơ sở demo của dự án CAMS.
        <br />
        Địa chỉ thực tế chưa được cung cấp.
      </p>
      <hr />
      <strong>GIÁ KHÁM:</strong>
      <p>Chưa công bố trong bản trải nghiệm.</p>
      <small>Không thu phí hay thanh toán trực tuyến trên bản demo.</small>
    </aside>
  );
}
export default function Schedule({
  doctor,
  compact = false,
}: {
  doctor: Doctor;
  compact?: boolean;
}) {
  const { catalog } = useApp();
  const [day, setDay] = useState(catalog!.now.slice(0, 10)),
    [slots, setSlots] = useState<Slot[]>([]),
    [error, setError] = useState(''),
    [loading, setLoading] = useState(true),
    [retry, setRetry] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    api<{ slots: Slot[] }>(
      `/api/public/slots?doctor_id=${doctor.id}&day=${day}`,
      'GET',
      undefined,
      controller.signal,
    )
      .then((r) => setSlots(r.slots))
      .catch((e) => {
        if (e.name !== 'AbortError') setError(e.message);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [doctor.id, day, retry]);
  return (
    <div className={'booking-block ' + (compact ? 'compact' : '')}>
      <div className="schedule-column">
        <label className="date-select">
          <span className="sr-only">Ngày khám của {doctor.name}</span>
          <select value={day} onChange={(e) => setDay(e.target.value)}>
            {Array.from({ length: 31 }, (_, i) => {
              const value = dayOffset(catalog!.now, i);
              return (
                <option key={value} value={value}>
                  {new Date(value + 'T12:00:00+07:00').toLocaleDateString('vi-VN', {
                    weekday: 'long',
                    day: '2-digit',
                    month: '2-digit',
                  })}
                </option>
              );
            })}
          </select>
        </label>
        <h3 className="schedule-title">
          <CalendarDays size={18} /> LỊCH KHÁM
        </h3>
        {loading ? (
          <Loading />
        ) : error ? (
          <ErrorBox message={error} retry={() => setRetry((x) => x + 1)} />
        ) : slots.length ? (
          <div className="time-slots">
            {slots.map((s) => (
              <Link
                key={s.id}
                className="time-slot"
                to={`/dat-lich-kham/${doctor.id}?slot=${s.id}&day=${day}`}
              >
                {s.starts_at.slice(11, 16)} - {s.ends_at.slice(11, 16)}
              </Link>
            ))}
          </div>
        ) : (
          <p className="slot-empty">Không còn giờ trống. Vui lòng chọn ngày khác.</p>
        )}
        <p className="schedule-footnote">
          Chọn <MousePointerClick size={15} /> và đặt lịch khám.
        </p>
        <p className="booking-note">
          Lịch trực tuyến cần được lễ tân xác nhận.{' '}
          {catalog?.clock_mode === 'demo' && `Ngày demo: ${catalog.now.slice(0, 10)}.`}
        </p>
      </div>
      <AddressBlock />
    </div>
  );
}
