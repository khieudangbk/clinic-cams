package com.example.cams.config.json;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Allows omitted fields but rejects explicit null and non-string values. */
public class NonNullTextDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            return parser.getString();
        }
        return context.reportInputMismatch(String.class, "Cần giá trị chuỗi.");
    }

    @Override
    public String getNullValue(DeserializationContext context) {
        return context.reportInputMismatch(String.class, "Giá trị chuỗi không được là null.");
    }

    @Override
    public String getAbsentValue(DeserializationContext context) {
        return null;
    }
}
