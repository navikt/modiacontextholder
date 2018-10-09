package no.nav.sbl.kafka;

import lombok.SneakyThrows;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.rest.domain.RSNyContext;

import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;
import static no.nav.sbl.util.EnvironmentUtils.requireEnvironmentName;


public class KafkaUtil {

    static String getAktivEnhetTopic() {
        return String.format("aapen-modia-nyAktivEnhet-v1-%s", requireEnvironmentName());
    }

    static String getAktivBrukerTopic() {
        return String.format("aapen-modia-nyAktivBruker-v1-%s", requireEnvironmentName());
    }

    @SneakyThrows
    public static String asTopic(RSNyContext nyContext) {
        EventType eventType = EventType.valueOf(nyContext.eventType);

        String topic;
        switch (eventType) {
            case NY_AKTIV_BRUKER: topic = getAktivBrukerTopic(); break;
            case NY_AKTIV_ENHET: topic = getAktivEnhetTopic(); break;
            default:
                throw new IllegalStateException(String.format("EventType %s er ikke gyldig", eventType));
        }
        return topic;
    }

    static String getKafkaBrokersUrl() {
        return getRequiredProperty("KAFKA_BROKERS_URL");
    }

}
