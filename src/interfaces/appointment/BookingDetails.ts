export interface BookingDetails {
  booking_for?: 'self' | 'relative';
  name?: string;
  phone?: string;
  birth_year?: string;
  gender?: '' | 'male' | 'female' | 'other';
  province?: string;
  ward?: string;
  address?: string;
  extra_note?: string;
  payment?: 'at_clinic';
}
