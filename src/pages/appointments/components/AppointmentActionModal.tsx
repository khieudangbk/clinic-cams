import type { AppointmentResponse } from '../../../interfaces/appointment/AppointmentResponse';
import type { TransitionRequest } from '../../../interfaces/appointment/TransitionRequest';
import type { SlotsResponse } from '../../../interfaces/slot/SlotsResponse';
import { useEffect, useState } from 'react';
import { api } from '../../../api';
import { useApp } from '../../../context';
import type { Slot } from '../../../interfaces/slot/Slot';
import { Loading, Modal } from '../../../components/UI';
import type { AppointmentActionModalProps } from '../../../interfaces/pages/AppointmentActionModalProps';
export function AppointmentActionModal({
  action,
  busy,
  onBusyChange,
  onClose,
  onUpdated,
}: AppointmentActionModalProps) {
  const { catalog, notify } = useApp();
  const [day, setDay] = useState(catalog!.now.slice(0, 10));
  const [slots, setSlots] = useState<Slot[]>([]);
  const [slotLoading, setSlotLoading] = useState(false);
  useEffect(() => {
    if (action?.name !== 'reschedule') return;
    const c = new AbortController();
    setSlotLoading(true);
    api<SlotsResponse>(
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
  return (
    <Modal
      title={
        action.name === 'reschedule'
          ? `Đổi lịch #${action.row.id}`
          : `${action.name === 'reject' ? 'Từ chối' : 'Hủy'} lịch #${action.row.id}`
      }
      onClose={() => {
        if (!busy) onClose();
      }}
    >
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          onBusyChange(true);
          const data = new FormData(e.currentTarget);
          try {
            await api<AppointmentResponse, TransitionRequest>(
              `/api/appointments/${action.row.id}/${action.name}`,
              'POST',
              {
                reason: String(data.get('reason') ?? ''),
                ...(data.get('slot_id') ? { slot_id: Number(data.get('slot_id')) } : {}),
              },
            );
            onClose();
            notify('Đã cập nhật lịch hẹn.');
            await onUpdated();
          } catch (err) {
            notify((err as Error).message, true);
          } finally {
            onBusyChange(false);
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
  );
}
