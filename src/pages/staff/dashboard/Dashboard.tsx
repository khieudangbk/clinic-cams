import { AppointmentTrend } from './components/AppointmentTrend';
import { AppointmentStatusChart } from './components/AppointmentStatusChart';
import { TodayQueue } from './components/TodayQueue';
import { DoctorRanking } from './components/DoctorRanking';
import type { AppointmentListResponse } from '../../../interfaces/appointment/AppointmentListResponse';
import { summarizeAppointments, csvCell } from '../../../utils';
import { useEffect, useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { api } from '../../../api';
import { useApp } from '../../../context';
import { dateText, dayOffset } from '../../../display';
import type { Appointment } from '../../../interfaces/appointment/Appointment';
import { ErrorBox, Loading } from '../../../components/UI';

export function Dashboard() {
  const { catalog } = useApp();
  const [rows, setRows] = useState<Appointment[]>([]),
    [range, setRange] = useState('7'),
    [doctor, setDoctor] = useState(''),
    [loading, setLoading] = useState(true),
    [error, setError] = useState('');
  const refresh = () => {
    setError('');
    api<AppointmentListResponse>('/api/appointments')
      .then((r) => setRows(r.appointments))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };
  useEffect(refresh, []);
  if (loading) return <Loading />;
  const today = catalog!.now.slice(0, 10);
  const { filtered, queue } = summarizeAppointments(rows, today, range, doctor);
  const count = (s: string) => filtered.filter((x) => x.status === s).length;
  const days = Array.from({ length: range === '30' ? 30 : 7 }, (_, i) =>
    dayOffset(today, i - (range === '30' ? 29 : 6)),
  );
  const doctorNames = [...new Map(rows.map((x) => [x.doctor_id, x.doctor_name])).entries()];
  const total = filtered.length;
  const csv = () => {
    const cell = csvCell;
    const lines = [
      ['Ngày khám', 'Tổng lịch', 'Hoàn thành'],
      ...[...new Set(filtered.flatMap((x) => (x.starts_at ? [x.starts_at.slice(0, 10)] : [])))]
        .sort()
        .map((day) => [
          day,
          filtered.filter((x) => x.starts_at?.startsWith(day)).length,
          filtered.filter((x) => x.starts_at?.startsWith(day) && x.status === 'completed').length,
        ]),
    ];
    const url = URL.createObjectURL(
      new Blob(['\ufeff' + lines.map((r) => r.map(cell).join(',')).join('\r\n')], {
        type: 'text/csv;charset=utf-8',
      }),
    );
    const link = document.createElement('a');
    link.href = url;
    link.download = `CAMS-${today}.csv`;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  };
  return (
    <>
      <div className="staff-title">
        <div>
          <h1>Tổng quan phòng khám</h1>
          <p>Thống kê từ lịch hẹn hiện có · Theo ngày khám</p>
        </div>
        <span className="staff-date">{dateText(catalog!.now)}</span>
      </div>
      <div className="dashboard-welcome">
        <h2>Mỗi lịch hẹn, một hành trình chăm sóc</h2>
        <p>Theo dõi hoạt động và tiếp nhận bệnh nhân trong ngày.</p>
      </div>
      <div className="appointment-toolbar">
        <div className="range-switch">
          {[
            ['7', '7 ngày'],
            ['30', '30 ngày'],
            ['all', 'Tất cả'],
          ].map(([v, l]) => (
            <button className={range === v ? 'active' : ''} key={v} onClick={() => setRange(v)}>
              {l}
            </button>
          ))}
        </div>
        <label>
          <span className="sr-only">Lọc bác sĩ</span>
          <select value={doctor} onChange={(e) => setDoctor(e.target.value)}>
            <option value="">Tất cả bác sĩ</option>
            {doctorNames.map(([id, name]) => (
              <option key={id} value={id ?? ''}>
                {name}
              </option>
            ))}
          </select>
        </label>
        <button className="outline" onClick={refresh}>
          <RefreshCw size={15} />
          Làm mới
        </button>
        <button className="outline" onClick={csv} disabled={!total}>
          Xuất CSV
        </button>
      </div>
      {error && <ErrorBox message={error} retry={refresh} />}
      <div className="stat-grid four">
        {[
          ['Tổng lịch trong kỳ', total],
          ['Hoàn thành', count('completed')],
          ['Cần tiếp nhận hôm nay', queue.length],
          ['Hủy / vắng mặt', count('cancelled') + count('noshow')],
        ].map(([t, v]) => (
          <div key={t}>
            <small>{t}</small>
            <strong>{v}</strong>
          </div>
        ))}
      </div>
      <div className="dashboard-grid">
        <AppointmentTrend filtered={filtered} days={days} range={range} />
        <AppointmentStatusChart filtered={filtered} />
        <TodayQueue queue={queue} />
        <DoctorRanking filtered={filtered} doctorNames={doctorNames} />
      </div>
    </>
  );
}
