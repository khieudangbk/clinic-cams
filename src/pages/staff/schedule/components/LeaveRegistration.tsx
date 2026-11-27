import type { AffectedAppointment } from '../../../../interfaces/leave/AffectedAppointment';
import type { CreateLeaveRequest } from '../../../../interfaces/leave/CreateLeaveRequest';
import type { CreateLeaveResponse } from '../../../../interfaces/leave/CreateLeaveResponse';
import { useState } from 'react';
import { api } from '../../../../api';
import { useApp } from '../../../../context';
import { dateText } from '../../../../display';
import type { LeaveRegistrationProps } from '../../../../interfaces/pages/LeaveRegistrationProps';

export function LeaveRegistration({ doctor, onSaved }: LeaveRegistrationProps) {
  const { catalog, notify } = useApp();
  const [affected, setAffected] = useState<AffectedAppointment[] | null>(null);
  return (
    <section className="panel">
      <h2>Đăng ký ngày nghỉ</h2>
      <p className="muted">
        Chặn đặt mới ngay. Lịch đã đặt được liệt kê để lễ tân liên hệ, không tự động hủy.
      </p>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          const d = Object.fromEntries(new FormData(e.currentTarget));
          try {
            const r = await api<CreateLeaveResponse, CreateLeaveRequest>('/api/leaves', 'POST', {
              doctor_id: Number(doctor),
              starts_at: d.starts_at + ':00+07:00',
              ends_at: d.ends_at + ':00+07:00',
              reason: String(d.reason),
            });
            setAffected(r.affected);
            notify(r.message);
            onSaved();
          } catch (err) {
            notify((err as Error).message, true);
          }
        }}
      >
        <label>
          Từ lúc
          <input
            name="starts_at"
            type="datetime-local"
            defaultValue={catalog!.now.slice(0, 10) + 'T09:00'}
            required
          />
        </label>
        <label>
          Đến lúc
          <input
            name="ends_at"
            type="datetime-local"
            defaultValue={catalog!.now.slice(0, 10) + 'T17:00'}
            required
          />
        </label>
        <label>
          Lý do
          <input name="reason" required maxLength={300} />
        </label>
        <button className="primary">Ghi nhận ngày nghỉ</button>
      </form>
      {affected && (
        <div className="notice">
          <strong>{affected.length} lịch bị ảnh hưởng</strong>
          {affected.map((a) => (
            <p key={a.id}>
              #{a.id} · {a.patient_name} · {a.patient_phone} · {dateText(a.starts_at)}
            </p>
          ))}
        </div>
      )}
    </section>
  );
}
