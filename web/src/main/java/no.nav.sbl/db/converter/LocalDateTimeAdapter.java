package no.nav.sbl.db.converter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    @Override
    public LocalDateTime unmarshal(String dateTime) throws Exception {
        return ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(ZoneOffset.systemDefault())
                .toLocalDateTime();
    }

    @Override
    public String marshal(LocalDateTime dateTime) throws Exception {
        return marshalLocalDateTime(dateTime);
    }

    public static String marshalLocalDateTime(LocalDateTime dateTime) throws Exception {
        return dateTime.atZone(ZoneOffset.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }
}
