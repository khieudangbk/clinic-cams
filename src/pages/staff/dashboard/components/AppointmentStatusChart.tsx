import { statuses } from '../../../../display';
import type { AppointmentStatusChartProps } from '../../../../interfaces/pages/AppointmentStatusChartProps';

export function AppointmentStatusChart({ filtered }: AppointmentStatusChartProps) {
  const total = filtered.length;
  const count = (status: string) => filtered.filter((row) => row.status === status).length;
  let offset = 0;
  const colors = ['#ffc10e', '#45c3d2', '#34929e', '#4e97d1', '#258367', '#d88187'];
  return (
    <section className="panel">
      <h2>Trạng thái lịch hẹn</h2>
      <div className="status-chart">
        <div className="donut">
          <svg viewBox="0 0 160 160" aria-label={`${total} lịch trong kỳ`} role="img">
            <circle cx="80" cy="80" r="60" fill="none" stroke="#edf3f5" strokeWidth="18" />
            {Object.keys(statuses).map((key, i) => {
              const length = total ? (count(key) / total) * 376.991 : 0;
              const at = offset;
              offset += length;
              return (
                <circle
                  key={key}
                  cx="80"
                  cy="80"
                  r="60"
                  fill="none"
                  stroke={colors[i]}
                  strokeWidth="18"
                  strokeDasharray={`${length} ${376.991 - length}`}
                  strokeDashoffset={-at}
                  transform="rotate(-90 80 80)"
                />
              );
            })}
          </svg>
          <strong>
            {total}
            <small>lịch hẹn</small>
          </strong>
        </div>
        <div className="status-counts">
          {Object.entries(statuses).map(([s, l], i) => (
            <div key={s}>
              <span>
                <i style={{ background: colors[i] }} />
                {l}
              </span>
              <strong>{count(s)}</strong>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
