import type { SaveScheduleRequest } from '../../../../interfaces/schedule/SaveScheduleRequest';
import type { SaveScheduleResponse } from '../../../../interfaces/schedule/SaveScheduleResponse';
import { api } from '../../../../api';
import { useApp } from '../../../../context';
import type { WeeklyScheduleProps } from '../../../../interfaces/pages/WeeklyScheduleProps';

export function WeeklySchedule({ current, manager, doctor, onSaved }: WeeklyScheduleProps) {
  const { refresh, notify } = useApp();
  return (
    <section className="panel">
      <h2>Mẫu lịch tuần</h2>
      <p>
        {current
          ? `${current.start_time}–${current.end_time} · Thứ ${current.weekdays
              .split(',')
              .map((x) => Number(x) + 2)
              .join(', ')}`
          : 'Chưa có mẫu lịch'}
      </p>
      {manager && (
        <form
          key={doctor}
          onSubmit={async (e) => {
            e.preventDefault();
            const d = Object.fromEntries(new FormData(e.currentTarget));
            try {
              await api<SaveScheduleResponse, SaveScheduleRequest>('/api/schedules', 'POST', {
                doctor_id: Number(doctor),
                weekdays: String(d.weekdays).split(',').map(Number),
                start_time: String(d.start_time),
                end_time: String(d.end_time),
              });
              notify('Đã lưu mẫu tuần.');
              onSaved();
              await refresh();
            } catch (err) {
              notify((err as Error).message, true);
            }
          }}
        >
          <label>
            Thứ làm việc (0=Hai, 5=Bảy)
            <input name="weekdays" defaultValue={current?.weekdays || '0,1,2,3,4,5'} required />
          </label>
          <div className="form-grid">
            <label>
              Bắt đầu
              <input
                name="start_time"
                type="time"
                min="07:00"
                max="17:00"
                defaultValue={current?.start_time || '07:00'}
                required
              />
            </label>
            <label>
              Kết thúc
              <input
                name="end_time"
                type="time"
                min="07:00"
                max="17:00"
                defaultValue={current?.end_time || '17:00'}
                required
              />
            </label>
          </div>
          <button className="primary">Lưu mẫu tuần</button>
        </form>
      )}
    </section>
  );
}
