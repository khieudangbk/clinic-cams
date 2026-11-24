package com.example.cams.entity;

import com.example.cams.entity.enums.AppointmentStatus;
import com.example.cams.entity.enums.BookingChannel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "cams_appointments")
public class CamsAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "reschedule_count", nullable = false)
    private int rescheduleCount = 0;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // JSON-encoded subset of {booking_for,name,phone,birth_year,gender,province,ward,address,extra_note,payment}.
    @Column(name = "booking_details", nullable = false, columnDefinition = "text")
    private String bookingDetails = "";

    @Column(name = "patient_name_override", nullable = false, length = 100)
    private String patientNameOverride = "";

    @Column(name = "patient_phone_override", nullable = false, length = 20)
    private String patientPhoneOverride = "";

    // SC7: set once the reminder email has been handled; reset to null when the appointment is rescheduled.
    @Column(name = "reminder_sent_at")
    private OffsetDateTime reminderSentAt;
}
