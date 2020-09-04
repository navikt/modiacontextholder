package no.nav.sbl.kafka;

import lombok.SneakyThrows;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.rest.domain.RSNyContext;



public class KafkaUtil {

    static String getAktivEnhetTopic() {
        return String.format("aapen-modia-nyAktivEnhet-v1-%s", EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME"));
    }

    static String getAktivBrukerTopic() {
        return String.format("aapen-modia-nyAktivBruker-v1-%s", EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME"));
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
        return EnvironmentUtils.getRequiredProperty("KAFKA_BROKERS_URL");
    }

}
