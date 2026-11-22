import type { DoctorRankingProps } from '../../../../interfaces/pages/DoctorRankingProps';

export function DoctorRanking({ filtered, doctorNames }: DoctorRankingProps) {
  const total = filtered.length;
  return (
    <section className="panel">
      <h2>Lịch hẹn theo bác sĩ</h2>
      {doctorNames.map(([id, name]) => {
        const n = filtered.filter((x) => x.doctor_id === id).length;
        return (
          <div className="doctor-ranking" key={id}>
            <span>
              {name}
              <strong>{n}</strong>
            </span>
            <div>
              <i style={{ width: total ? (n / total) * 100 + '%' : '0%' }} />
            </div>
          </div>
        );
      })}
      {!doctorNames.length && <p>Chưa có dữ liệu.</p>}
      <p className="muted">
        Trực tuyến: {filtered.filter((x) => x.channel === 'online').length} · Lễ tân:{' '}
        {filtered.filter((x) => x.channel === 'staff').length}
      </p>
    </section>
  );
}
