package com.example.cams.config.json;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Preserves the admin API's support for booleans and numeric flags 0/1. */
public class ActiveFlagDeserializer extends ValueDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.hasToken(JsonToken.VALUE_TRUE) || parser.hasToken(JsonToken.VALUE_FALSE)) {
            return parser.getBooleanValue();
        }
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            long flag = parser.getLongValue();
            if (flag == 0 || flag == 1) {
                return flag == 1;
            }
        }
        return context.reportInputMismatch(Boolean.class, "Trạng thái tài khoản phải là true, false, 0 hoặc 1.");
    }
}
