import type { ReactElement } from 'react';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { afterEach, vi } from 'vitest';
import { cleanup, render } from '@testing-library/react';
import type { Store } from '../interfaces/app/Store';
import type { User } from '../interfaces/account/User';
import type { Slot } from '../interfaces/slot/Slot';
import type { Appointment } from '../interfaces/appointment/Appointment';

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

export const patient: User = {
  id: 7,
  name: 'Nguyen An',
  email: 'an@example.com',
  phone: '0912345678',
  role: 'patient',
  active: 1,
  specialty_id: null,
};
export const slot: Slot = {
  id: 42,
  doctor_id: 2,
  starts_at: '2026-11-24T09:00+07:00',
  ends_at: '2026-11-24T09:30+07:00',
};
export const appointment: Appointment = {
  id: 10,
  patient_id: patient.id,
  slot_id: slot.id,
  doctor_id: 2,
  starts_at: slot.starts_at,
  ends_at: slot.ends_at,
  doctor_name: 'BS Binh',
  patient_name: patient.name,
  patient_phone: patient.phone,
  status: 'pending',
  channel: 'online',
  reason: 'Kham tong quat',
  reschedule_count: 0,
  created_by: patient.id,
  created_at: '2026-11-23T08:00+07:00',
  doctor_on_leave: 0,
  booking_details: '',
};

export function makeStore(overrides: Partial<Store> = {}): Store {
  return {
    catalog: {
      specialties: [
        { id: 1, name: 'Tim mach' },
        { id: 2, name: 'Nhi khoa' },
      ],
      doctors: [{ id: 2, name: 'BS Binh', specialty_id: 1, specialty_name: 'Tim mach' }],
      now: '2026-11-23T08:00+07:00',
      clock_mode: 'demo',
    },
    user: patient,
    settings: { slot_minutes: 30, cutoff_hours: 2, max_reschedules: 2 },
    ready: true,
    error: '',
    refresh: vi.fn(async () => {}),
    logout: vi.fn(async () => {}),
    notify: vi.fn(),
    ...overrides,
  };
}

function LocationProbe() {
  const location = useLocation();
  return <output data-testid="location">{location.pathname + location.search}</output>;
}

export function renderPage(element: ReactElement, path = '/') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      {element}
      <LocationProbe />
    </MemoryRouter>,
  );
}
