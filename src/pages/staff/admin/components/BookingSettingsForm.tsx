import { useApp } from '../../../../context';
import type { BookingSettingsFormProps } from '../../../../interfaces/pages/BookingSettingsFormProps';

export function BookingSettingsForm({ save }: BookingSettingsFormProps) {
  const { settings } = useApp();
  return (
    <section className="panel">
      <h2>Quy tắc đặt lịch</h2>
      <form
        key={JSON.stringify(settings)}
        onSubmit={(e) => {
          e.preventDefault();
          const data = new FormData(e.currentTarget);
          save('/api/settings', {
            slot_minutes: Number(data.get('slot_minutes')),
            cutoff_hours: Number(data.get('cutoff_hours')),
            max_reschedules: Number(data.get('max_reschedules')),
          });
        }}
      >
        {[
          ['slot_minutes', 'Độ dài slot mới (phút)', 5, 120],
          ['cutoff_hours', 'Đổi/hủy trước ít nhất (giờ)', 0, 72],
          ['max_reschedules', 'Số lần đổi tối đa', 0, 10],
        ].map(([key, label, min, max]) => (
          <label key={key}>
            {label}
            <input
              type="number"
              name={String(key)}
              min={Number(min)}
              max={Number(max)}
              defaultValue={settings[String(key)]}
              required
            />
          </label>
        ))}
        <button className="primary">Lưu quy tắc</button>
      </form>
      <p className="notice">
        Các slot đã sinh giữ độ dài cũ. Cấu hình mới áp dụng khi tạo slot cho ngày mới.
      </p>
    </section>
  );
}
