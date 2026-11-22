package com.example.cams.config.json;

import com.example.cams.util.CamsValidation;
import com.example.cams.util.exception.CamsApiException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Accepts integral JSON numbers and canonical strings such as "42". */
public class CanonicalLongDeserializer extends ValueDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return parser.getLongValue();
        }
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            try {
                return CamsValidation.integer(parser.getString(), "Số nguyên");
            } catch (CamsApiException exception) {
                return context.reportInputMismatch(Long.class, "Chuỗi số nguyên không hợp lệ.");
            }
        }
        return context.reportInputMismatch(Long.class, "Cần số nguyên hoặc chuỗi số nguyên hợp lệ.");
    }
}
