import type { AppointmentAction } from '../../../interfaces/appointment/AppointmentAction';
import { AppointmentActionModal } from './AppointmentActionModal';
import { AppointmentTable } from './AppointmentTable';
import { AuditHistoryModal } from './AuditHistoryModal';
import type { AuditEntry } from '../../../interfaces/appointment/AuditEntry';
import type { AuditResponse } from '../../../interfaces/appointment/AuditResponse';
import type { AppointmentResponse } from '../../../interfaces/appointment/AppointmentResponse';
import type { AppointmentListResponse } from '../../../interfaces/appointment/AppointmentListResponse';
import type { TransitionRequest } from '../../../interfaces/appointment/TransitionRequest';
import { useEffect, useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { api } from '../../../api';
import { useApp } from '../../../context';
import { statuses } from '../../../display';
import type { Appointment } from '../../../interfaces/appointment/Appointment';
import { ErrorBox, Loading } from '../../../components/UI';

export function AppointmentList({ staff = false }: { staff?: boolean }) {
  const { notify } = useApp();
  const [rows, setRows] = useState<Appointment[]>([]),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(''),
    [filter, setFilter] = useState(''),
    [query, setQuery] = useState(''),
    [action, setAction] = useState<AppointmentAction | null>(null),
    [audit, setAudit] = useState<AuditEntry[] | null>(null),
    [busy, setBusy] = useState(false);
  const refresh = async () => {
    setError('');
    try {
      const r = await api<AppointmentListResponse>('/api/appointments');
      setRows(r.appointments);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    refresh();
  }, []);

  const doAction = async (row: Appointment, name: string) => {
    if (name === 'audit') {
      try {
        setAudit((await api<AuditResponse>(`/api/appointments/${row.id}/audit`)).audit);
      } catch (e) {
        notify((e as Error).message, true);
      }
      return;
    }
    if (['cancel', 'reject', 'reschedule'].includes(name)) {
      setAction({ row, name });
      return;
    }
    setBusy(true);
    try {
      await api<AppointmentResponse, TransitionRequest>(
        `/api/appointments/${row.id}/${name}`,
        'POST',
        {},
      );
      notify('Đã cập nhật lịch hẹn.');
      await refresh();
    } catch (e) {
      notify((e as Error).message, true);
    } finally {
      setBusy(false);
    }
  };
  const visible = rows.filter(
    (x) =>
      (!filter || x.status === filter) &&
      `${x.id} ${x.patient_name} ${x.patient_phone} ${x.doctor_name}`
        .toLowerCase()
        .includes(query.toLowerCase()),
  );
  if (loading) return <Loading />;
  return (
    <>
      <div className="stat-grid">
        <div>
          <small>Chờ xác nhận</small>
          <strong>{rows.filter((x) => x.status === 'pending').length}</strong>
        </div>
        <div>
          <small>Sắp khám / đang tiếp nhận</small>
          <strong>{rows.filter((x) => ['confirmed', 'arrived'].includes(x.status)).length}</strong>
        </div>
        <div>
          <small>Đã hoàn thành</small>
          <strong>{rows.filter((x) => x.status === 'completed').length}</strong>
        </div>
      </div>
      <div className="appointment-toolbar">
        <label>
          <span className="sr-only">Tìm lịch hẹn</span>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={staff ? 'Tìm mã lịch, bệnh nhân, bác sĩ…' : 'Tìm mã lịch hoặc bác sĩ…'}
          />
        </label>
        <label>
          <span className="sr-only">Lọc trạng thái</span>
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="">Tất cả trạng thái</option>
            {Object.entries(statuses).map(([v, l]) => (
              <option value={v} key={v}>
                {l}
              </option>
            ))}
          </select>
        </label>
        <button className="outline" onClick={refresh}>
          <RefreshCw size={16} />
          Làm mới
        </button>
      </div>
      {error && <ErrorBox message={error} retry={refresh} />}
      <AppointmentTable visible={visible} staff={staff} busy={busy} onAction={doAction} />
      {action && (
        <AppointmentActionModal
          action={action}
          busy={busy}
          onBusyChange={setBusy}
          onClose={() => setAction(null)}
          onUpdated={refresh}
        />
      )}
      {audit && <AuditHistoryModal audit={audit} onClose={() => setAudit(null)} />}
    </>
  );
}
