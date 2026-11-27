import { WeeklySchedule } from './components/WeeklySchedule';
import { LeaveRegistration } from './components/LeaveRegistration';
import type { Leave } from '../../../interfaces/leave/Leave';
import type { LeaveListResponse } from '../../../interfaces/leave/LeaveListResponse';
import { useEffect, useState } from 'react';
import { api } from '../../../api';
import { useApp } from '../../../context';
import { dateText } from '../../../display';
import type { CatalogResponse } from '../../../interfaces/catalog/CatalogResponse';
import { Empty, ErrorBox, Loading } from '../../../components/UI';

export function StaffSchedule() {
  const { user, catalog } = useApp();
  const manager = user?.role === 'manager';
  const [data, setData] = useState<CatalogResponse | null>(null),
    [leaves, setLeaves] = useState<Leave[]>([]),
    [doctor, setDoctor] = useState(String(manager ? catalog!.doctors[0]?.id : user?.id)),
    [error, setError] = useState('');
  const load = () => {
    Promise.all([api<CatalogResponse>('/api/catalog'), api<LeaveListResponse>('/api/leaves')])
      .then(([c, l]) => {
        setData(c);
        setLeaves(l.leaves);
      })
      .catch((e) => setError(e.message));
  };
  useEffect(load, []);
  if (!['manager', 'doctor'].includes(user?.role || ''))
    return <Empty title="Không có quyền quản lý lịch bác sĩ" />;
  if (!data) return error ? <ErrorBox message={error} retry={load} /> : <Loading />;
  const current = data.schedules?.find((x) => x.doctor_id === Number(doctor));
  return (
    <>
      <h1>Lịch làm việc và ngày nghỉ</h1>
      {error && <ErrorBox message={error} />}
      <label className="doctor-picker">
        Bác sĩ
        <select value={doctor} onChange={(e) => setDoctor(e.target.value)}>
          {data.doctors
            .filter((d) => manager || d.id === user?.id)
            .map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
        </select>
      </label>
      <div className="dashboard-grid">
        <WeeklySchedule current={current} manager={manager} doctor={doctor} onSaved={load} />
        <LeaveRegistration doctor={doctor} onSaved={load} />
      </div>
      <section className="panel section-gap">
        <h2>Ngày nghỉ đã ghi nhận</h2>
        {leaves
          .filter((x) => x.doctor_id === Number(doctor))
          .map((x) => (
            <p key={x.id}>
              {dateText(x.starts_at)} → {dateText(x.ends_at)} · {x.reason}
            </p>
          ))}
        {!leaves.some((x) => x.doctor_id === Number(doctor)) && <p>Chưa đăng ký ngày nghỉ.</p>}
      </section>
    </>
  );
}
