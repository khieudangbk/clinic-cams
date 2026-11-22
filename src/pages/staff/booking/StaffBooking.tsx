import type { AppointmentResponse } from '../../../interfaces/appointment/AppointmentResponse';
import type { CreateAppointmentRequest } from '../../../interfaces/appointment/CreateAppointmentRequest';
import type { SlotsResponse } from '../../../interfaces/slot/SlotsResponse';
import { useEffect, useState } from 'react';
import { api } from '../../../api';
import { useApp } from '../../../context';
import type { Slot } from '../../../interfaces/slot/Slot';
import { Empty, ErrorBox } from '../../../components/UI';

export function StaffBooking() {
  const { user, catalog, notify } = useApp();
  const [doctor, setDoctor] = useState(String(catalog!.doctors[0]?.id || '')),
    [day, setDay] = useState(catalog!.now.slice(0, 10)),
    [slots, setSlots] = useState<Slot[]>([]),
    [slot, setSlot] = useState(''),
    [error, setError] = useState(''),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    setSlot('');
    const c = new AbortController();
    api<SlotsResponse>(`/api/slots?doctor_id=${doctor}&day=${day}`, 'GET', undefined, c.signal)
      .then((r) => setSlots(r.slots))
      .catch((e) => {
        if (e.name !== 'AbortError') setError(e.message);
      });
    return () => c.abort();
  }, [doctor, day]);
  if (user?.role !== 'receptionist') return <Empty title="Chức năng dành cho lễ tân" />;
  return (
    <>
      <h1>Đặt lịch hộ bệnh nhân</h1>
      <div className="panel content-narrow">
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            const form = e.currentTarget;
            const data = new FormData(form);
            setBusy(true);
            setError('');
            try {
              const r = await api<AppointmentResponse, CreateAppointmentRequest>(
                '/api/appointments',
                'POST',
                {
                  patient_name: String(data.get('patient_name') ?? ''),
                  patient_phone: String(data.get('patient_phone') ?? ''),
                  patient_email: String(data.get('patient_email') ?? ''),
                  reason: String(data.get('reason') ?? ''),
                  slot_id: Number(slot),
                },
              );
              notify(`Đã tạo và xác nhận lịch #${r.appointment.id}.`);
              setSlots((x) => x.filter((s) => s.id !== Number(slot)));
              setSlot('');
            } catch (err) {
              setError((err as Error).message);
            } finally {
              setBusy(false);
            }
          }}
        >
          <div className="form-grid">
            <label>
              Bác sĩ
              <select value={doctor} onChange={(e) => setDoctor(e.target.value)}>
                {catalog!.doctors.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name} · {d.specialty_name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Ngày khám
              <input
                type="date"
                min={catalog!.now.slice(0, 10)}
                value={day}
                onChange={(e) => setDay(e.target.value)}
              />
            </label>
            <label>
              Họ tên bệnh nhân
              <input name="patient_name" required maxLength={100} />
            </label>
            <label>
              Điện thoại
              <input name="patient_phone" type="tel" required pattern="[+0-9 ()-]{8,20}" />
            </label>
          </div>
          <label>
            Email tài khoản đã đăng ký (nếu có)
            <input name="patient_email" type="email" />
          </label>
          <label>
            Giờ khám
            <select required value={slot} onChange={(e) => setSlot(e.target.value)}>
              <option value="">Chọn khung giờ</option>
              {slots.map((s) => (
                <option value={s.id} key={s.id}>
                  {s.starts_at.slice(11, 16)} – {s.ends_at.slice(11, 16)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Lý do khám
            <textarea name="reason" required maxLength={500} />
          </label>
          {error && <ErrorBox message={error} />}
          <button className="primary full" disabled={busy || !slot}>
            {busy ? 'Đang tạo…' : 'Tạo lịch và xác nhận'}
          </button>
        </form>
      </div>
    </>
  );
}
