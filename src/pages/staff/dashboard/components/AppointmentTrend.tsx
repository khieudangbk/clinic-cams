import type { AppointmentTrendProps } from '../../../../interfaces/pages/AppointmentTrendProps';

export function AppointmentTrend({ filtered, days, range }: AppointmentTrendProps) {
  const max = Math.max(
    1,
    ...days.map((day) => filtered.filter((row) => row.starts_at?.startsWith(day)).length),
  );
  return (
    <section className="panel">
      <h2>Xu hướng lịch hẹn</h2>
      <p className="muted">
        {range === 'all'
          ? '7 ngày gần nhất trên biểu đồ; thẻ và CSV tính toàn bộ kỳ.'
          : 'Số lịch theo ngày trong kỳ'}
      </p>
      <div className="day-chart" role="img" aria-label="Biểu đồ tổng lịch theo ngày">
        {days.map((day) => {
          const n = filtered.filter((x) => x.starts_at?.startsWith(day)).length;
          return (
            <div key={day}>
              <span>{n || ''}</span>
              <div className="day-bar" style={{ height: (n / max) * 150 }} />
              <small>
                {day.slice(8)}/{day.slice(5, 7)}
              </small>
            </div>
          );
        })}
      </div>
    </section>
  );
}
