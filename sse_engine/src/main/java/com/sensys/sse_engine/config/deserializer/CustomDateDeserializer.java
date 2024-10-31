package com.sensys.sse_engine.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
public class CustomDateDeserializer extends JsonDeserializer<Date> {
    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "yyyy-MM-dd",
        "HH:mm:ss z"
    };

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String dateStr = parser.getText();
        
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                return dateFormat.parse(dateStr);
            } catch (ParseException ignored) {
                // Try next format
            }
        }
        
        log.warn("Failed to parse date: {}", dateStr);
        return new Date(); // Return current date as fallback
    }
}