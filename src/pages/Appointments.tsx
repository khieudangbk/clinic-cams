import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { RefreshCw } from 'lucide-react';
import { api } from '../api';
import { useApp } from '../context';
import { dateText, statuses, type Appointment, type Slot } from '../types';
import { Breadcrumb, Empty, ErrorBox, Loading, Modal } from '../components/UI';
export function AppointmentList({ staff = false }: { staff?: boolean }) {
  const { user, catalog, notify } = useApp();
  const [rows, setRows] = useState<Appointment[]>([]),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(''),
    [filter, setFilter] = useState(''),
    [query, setQuery] = useState(''),
    [action, setAction] = useState<{ row: Appointment; name: string } | null>(null),
    [busy, setBusy] = useState(false),
    [day, setDay] = useState(catalog!.now.slice(0, 10)),
    [slots, setSlots] = useState<Slot[]>([]),
    [slotLoading, setSlotLoading] = useState(false);
  const refresh = async () => {
    setError('');
    try {
      const r = await api<{ appointments: Appointment[] }>('/api/appointments');
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
  useEffect(() => {
    if (action?.name !== 'reschedule') return;
    const c = new AbortController();
    setSlotLoading(true);
    api<{ slots: Slot[] }>(
      `/api/slots?doctor_id=${action.row.doctor_id}&day=${day}`,
      'GET',
      undefined,
      c.signal,
    )
      .then((r) => setSlots(r.slots))
      .catch((e) => {
        if (e.name !== 'AbortError') notify(e.message, true);
      })
      .finally(() => {
        if (!c.signal.aborted) setSlotLoading(false);
      });
    return () => c.abort();
  }, [day, action]);
  const doAction = async (row: Appointment, name: string) => {
    if (['cancel', 'reschedule'].includes(name)) {
      setDay(catalog!.now.slice(0, 10));
      setAction({ row, name });
      return;
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
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Mã / Người khám</th>
              <th>Bác sĩ / Khung giờ</th>
              <th>Trạng thái</th>
              <th>Lý do khám</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((row) => (
              <tr key={row.id}>
                <td>
                  <strong>
                    #{row.id} · {row.patient_name}
                  </strong>
                  <small>
                    {row.patient_phone} ·{' '}
                    {row.channel === 'online' ? 'Trực tuyến' : 'Lễ tân đặt hộ'}
                  </small>
                  <small>Đã đổi {row.reschedule_count} lần</small>
                </td>
                <td>
                  {row.doctor_name}
                  <small>{dateText(row.starts_at)}</small>
                </td>
                <td>
                  <span className={'badge ' + row.status}>{statuses[row.status]}</span>
                  {!!row.doctor_on_leave &&
                    ['pending', 'confirmed', 'arrived'].includes(row.status) && (
                      <p className="notice">Bác sĩ nghỉ, cần liên hệ xử lý</p>
                    )}
                </td>
                <td>
                  {row.reason}
                </td>
                <td>
                  <div className="row-actions">
                    {['patient', 'receptionist'].includes(user?.role || '') &&
                      ['pending', 'confirmed'].includes(row.status) && (
                        <>
                          <button disabled={busy} onClick={() => doAction(row, 'reschedule')}>
                            Đổi lịch
                          </button>
                          <button
                            className="danger-text"
                            disabled={busy}
                            onClick={() => doAction(row, 'cancel')}
                          >
                            Hủy
                          </button>
                        </>
                      )}
                  </div>
                </td>
              </tr>
            ))}
            {!visible.length && (
              <tr>
                <td colSpan={5}>
                  <Empty title="Chưa có lịch hẹn phù hợp">
                    {!staff && <Link to="/bac-si">Tìm bác sĩ và đặt khám</Link>}
                  </Empty>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {action && (
        <Modal
          title={
            action.name === 'reschedule'
              ? `Đổi lịch #${action.row.id}`
              : `${action.name === 'reject' ? 'Từ chối' : 'Hủy'} lịch #${action.row.id}`
          }
          onClose={() => {
            if (!busy) setAction(null);
          }}
        >
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              const data = Object.fromEntries(new FormData(e.currentTarget));
              try {
                await api(`/api/appointments/${action.row.id}/${action.name}`, 'POST', {
                  ...data,
                  ...(data.slot_id ? { slot_id: Number(data.slot_id) } : {}),
                });
                setAction(null);
                notify('Đã cập nhật lịch hẹn.');
                await refresh();
              } catch (err) {
                notify((err as Error).message, true);
              } finally {
                setBusy(false);
              }
            }}
          >
            {action.name === 'reschedule' && (
              <>
                <label>
                  Ngày khám mới
                  <input
                    type="date"
                    min={catalog!.now.slice(0, 10)}
                    value={day}
                    onChange={(e) => setDay(e.target.value)}
                    required
                  />
                </label>
                <label>
                  Khung giờ mới
                  {slotLoading ? (
                    <Loading />
                  ) : (
                    <select name="slot_id" required defaultValue="">
                      <option value="" disabled>
                        Chọn giờ trống
                      </option>
                      {slots.map((s) => (
                        <option value={s.id} key={s.id}>
                          {s.starts_at.slice(11, 16)} – {s.ends_at.slice(11, 16)}
                        </option>
                      ))}
                    </select>
                  )}
                </label>
                {!slotLoading && !slots.length && <p>Không có giờ trống trong ngày này.</p>}
              </>
            )}
            <label>
              Lý do
              <textarea name="reason" required maxLength={300} />
            </label>
            <button
              className="primary full"
              disabled={busy || (action.name === 'reschedule' && (slotLoading || !slots.length))}
            >
              {busy ? 'Đang xử lý…' : 'Xác nhận'}
            </button>
          </form>
        </Modal>
      )}
    </>
  );
}
export default function Appointments() {
  const { user } = useApp();
  if (!user)
    return (
      <div className="container page auth-card">
        <h1>Lịch hẹn của tôi</h1>
        <p>Đăng nhập để xem và quản lý lịch khám của bạn.</p>
        <Link className="primary" to="/dang-nhap?next=/lich-hen">
          Đăng nhập
        </Link>
      </div>
    );
  if (user.role !== 'patient')
    return (
      <div className="container page">
        <h1>Lịch hẹn phòng khám</h1>
        <Link to="/nhan-vien/lich-hen" className="primary">
          Mở danh sách trong khu vực nhân viên
        </Link>
      </div>
    );
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Lịch hẹn của tôi' }]} />
      <div className="section-heading">
        <h1>Lịch hẹn của tôi</h1>
        <Link to="/bac-si" className="see-more">
          Đặt lịch mới
        </Link>
      </div>
      <AppointmentList />
    </div>
  );
}
