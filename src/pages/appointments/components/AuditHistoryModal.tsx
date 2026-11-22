import { dateText } from '../../../display';
import { Modal } from '../../../components/UI';
import type { AuditHistoryModalProps } from '../../../interfaces/pages/AuditHistoryModalProps';

export function AuditHistoryModal({ audit, onClose }: AuditHistoryModalProps) {
  return (
    <Modal title="Lịch sử xử lý lịch hẹn" onClose={() => onClose()}>
      <div className="audit-list">
        {audit.map((x) => (
          <article key={x.id}>
            <strong>
              {(
                {
                  create: 'Đặt lịch',
                  reschedule: 'Đổi lịch',
                  approve: 'Xác nhận',
                  reject: 'Từ chối',
                  cancel: 'Hủy lịch',
                  checkin: 'Tiếp nhận',
                  complete: 'Hoàn thành',
                  noshow: 'Vắng mặt',
                } as Record<string, string>
              )[x.action] || x.action}{' '}
              · {x.actor_name}
            </strong>
            <small>{dateText(x.created_at)}</small>
            <p>
              {x.reason.replace(
                /Chuyển trạng thái (confirmed|arrived|completed|noshow)/g,
                (_: string, status: string) =>
                  (
                    ({
                      confirmed: 'Lễ tân đã xác nhận lịch hẹn',
                      arrived: 'Bệnh nhân đã được tiếp nhận',
                      completed: 'Buổi khám đã hoàn thành',
                      noshow: 'Ghi nhận bệnh nhân vắng mặt',
                    }) as Record<string, string>
                  )[status],
              )}
            </p>
          </article>
        ))}
      </div>
    </Modal>
  );
}
