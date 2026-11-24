package com.example.cams.service.event;

/** Published inside the booking transaction whenever an appointment becomes (or is re-timed while) confirmed. */
public record AppointmentConfirmedEvent(long appointmentId) {
}
