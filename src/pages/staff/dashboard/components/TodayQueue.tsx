import { Link } from 'react-router-dom';
import { statuses } from '../../../../display';
import type { TodayQueueProps } from '../../../../interfaces/pages/TodayQueueProps';

export function TodayQueue({ queue }: TodayQueueProps) {
  return (
    <section className="panel">
      <h2>Hàng đợi hôm nay</h2>
      {queue.length ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Giờ</th>
                <th>Người khám</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {queue.slice(0, 8).map((x) => (
                <tr key={x.id}>
                  <td>{x.starts_at?.slice(11, 16) ?? '—'}</td>
                  <td>{x.patient_name}</td>
                  <td>
                    <span className={'badge ' + x.status}>{statuses[x.status]}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="empty-state">Không có lịch chờ hôm nay.</p>
      )}
      <Link className="see-more-link" to="/nhan-vien/lich-hen">
        Xem toàn bộ lịch hẹn →
      </Link>
    </section>
  );
}
