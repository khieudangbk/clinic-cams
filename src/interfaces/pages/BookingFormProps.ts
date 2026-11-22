import type { User } from '../account/User';
import type { Slot } from '../slot/Slot';

export interface BookingFormProps {
  slot: Slot;
  user: User;
}
