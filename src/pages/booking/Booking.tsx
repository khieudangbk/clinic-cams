import { BookingForm } from './components/BookingForm';
import type { SlotsResponse } from '../../interfaces/slot/SlotsResponse';
import { useEffect, useState } from 'react';
import { Link, useLocation, useParams, useSearchParams } from 'react-router-dom';
import { CalendarDays } from 'lucide-react';
import { api } from '../../api';
import { useApp } from '../../context';
import { dateText } from '../../display';
import type { Slot } from '../../interfaces/slot/Slot';
import { media, doctorImage } from '../../data';
import { Breadcrumb, ErrorBox, Loading, Empty } from '../../components/UI';

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
    api<SlotsResponse>(
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
  return (
    <div className="container booking-form">
      <Breadcrumb
        items={[
          { label: 'Bác sĩ', to: '/bac-si' },
          { label: d.name, to: '/bac-si/' + id },
          { label: 'Đặt khám' },
        ]}
      />
      <div className="booking-summary">
        <img src={media + doctorImage(d)} alt="" />
        <div>
          <p>Đặt lịch khám</p>
          <h1>{d.name}</h1>
          <p className="booking-time">
            <CalendarDays size={17} />
            {dateText(slot!.starts_at)} — {slot!.ends_at.slice(11, 16)}
          </p>
          <p>Phòng khám CAMS · {d.specialty_name}</p>
          <small>Giá khám: chưa công bố trong demo</small>
        </div>
      </div>
      <BookingForm slot={slot!} user={user} />
    </div>
  );
}
