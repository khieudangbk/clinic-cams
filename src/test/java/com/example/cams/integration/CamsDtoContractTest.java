package com.example.cams.integration;

import com.example.cams.config.JacksonConfig;
import com.example.cams.dto.request.account.LoginRequest;
import com.example.cams.dto.request.account.RegisterRequest;
import com.example.cams.dto.request.admin.UpdateSettingsRequest;
import com.example.cams.dto.request.admin.UpdateStaffUserRequest;
import com.example.cams.dto.request.appointment.CreateAppointmentRequest;
import com.example.cams.dto.request.schedule.SaveScheduleRequest;
import com.example.cams.dto.response.slot.SlotInfo;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CamsDtoContractTest {

    private final JsonMapper mapper;

    CamsDtoContractTest() {
        var builder = JsonMapper.builder();
        new JacksonConfig().camsJsonTypes().customize(builder);
        mapper = builder.build();
    }

    @Test
    void distinguishesOmittedFieldsFromExplicitNull() {
        assertNull(mapper.readValue("{}", RegisterRequest.class).phone());
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"phone\":null}", RegisterRequest.class));

        var partial = mapper.readValue("{\"active\":false}", UpdateStaffUserRequest.class);
        var clear = mapper.readValue("{\"specialty_id\":null,\"active\":1}", UpdateStaffUserRequest.class);
        assertEquals(false, partial.getActive());
        assertFalse(partial.isSpecialtyIdProvided());
        assertFalse(mapper.readValue("{\"specialtyIdProvided\":true}", UpdateStaffUserRequest.class)
                .isSpecialtyIdProvided());
        assertTrue(clear.isSpecialtyIdProvided());
        assertNull(clear.getSpecialtyId());
        assertEquals(true, clear.getActive());
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"active\":2}", UpdateStaffUserRequest.class));
    }

    @Test
    void bindsConcreteTypesWithoutCoercingInvalidValues() {
        var numericString = mapper.readValue("{\"slot_id\":\"7\"}", CreateAppointmentRequest.class);
        assertEquals(7L, numericString.slotId());
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"slot_id\":7.0}", CreateAppointmentRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"slot_id\":\"07\"}", CreateAppointmentRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"email\":123}", LoginRequest.class));
        assertThrows(DatabindException.class,
                () -> mapper.readValue("{\"weekdays\":[\"2\"]}", SaveScheduleRequest.class));

        var settings = mapper.readValue("{\"slot_minutes\":30,\"unsupported\":1}", UpdateSettingsRequest.class);
        assertEquals(30L, settings.values().get("slot_minutes"));
        assertTrue(settings.values().containsKey("unsupported"));
    }

    @Test
    void bindsBookingDetailsAndRejectsUnknownFields() {
        var booking = mapper.readValue("""
                {"booking_details":{"booking_for":"self","name":"Test","phone":"0912345678"}}
                """, CreateAppointmentRequest.class);
        assertEquals("self", booking.bookingDetails().bookingFor());
        assertNull(booking.bookingDetails().birthYear());
        assertThrows(DatabindException.class, () -> mapper.readValue(
                "{\"booking_details\":{\"unsupported\":\"test\"}}", CreateAppointmentRequest.class));
    }

    @Test
    void omitsDoctorIdOnlyForPublicSlots() {
        var publicSlot = mapper.valueToTree(new SlotInfo(10, null, "start", "end"));
        var privateSlot = mapper.valueToTree(new SlotInfo(10, 7L, "start", "end"));
        assertFalse(publicSlot.has("doctor_id"));
        assertEquals(7L, privateSlot.get("doctor_id").asLong());
        assertEquals("start", publicSlot.get("starts_at").asText());
    }
}
