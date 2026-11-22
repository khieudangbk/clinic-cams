import { Link } from 'react-router-dom';
import { useApp } from '../../../context';
import { dateText, statuses } from '../../../display';
import { Empty } from '../../../components/UI';
import { BookingDetails } from './BookingDetails';
import type { AppointmentTableProps } from '../../../interfaces/pages/AppointmentTableProps';

export function AppointmentTable({ visible, staff, busy, onAction }: AppointmentTableProps) {
  const { user } = useApp();
  return (
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
                  {row.patient_phone} · {row.channel === 'online' ? 'Trực tuyến' : 'Lễ tân đặt hộ'}
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
                {row.booking_details && (
                  <details>
                    <summary>Thông tin người khám</summary>
                    <BookingDetails value={row.booking_details} />
                  </details>
                )}
              </td>
              <td>
                <div className="row-actions">
                  {user?.role === 'receptionist' && (
                    <>
                      {row.status === 'pending' && (
                        <>
                          <button disabled={busy} onClick={() => onAction(row, 'approve')}>
                            Xác nhận
                          </button>
                          <button disabled={busy} onClick={() => onAction(row, 'reject')}>
                            Từ chối
                          </button>
                        </>
                      )}
                      {row.status === 'confirmed' && (
                        <>
                          <button disabled={busy} onClick={() => onAction(row, 'checkin')}>
                            Check-in
                          </button>
                          <button disabled={busy} onClick={() => onAction(row, 'noshow')}>
                            Vắng mặt
                          </button>
                        </>
                      )}
                      {row.status === 'arrived' && (
                        <button disabled={busy} onClick={() => onAction(row, 'complete')}>
                          Hoàn thành
                        </button>
                      )}
                    </>
                  )}
                  {['patient', 'receptionist'].includes(user?.role || '') &&
                    ['pending', 'confirmed'].includes(row.status) && (
                      <>
                        <button disabled={busy} onClick={() => onAction(row, 'reschedule')}>
                          Đổi lịch
                        </button>
                        <button
                          className="danger-text"
                          disabled={busy}
                          onClick={() => onAction(row, 'cancel')}
                        >
                          Hủy
                        </button>
                      </>
                    )}
                  <button onClick={() => onAction(row, 'audit')}>Lịch sử</button>
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
  );
}
