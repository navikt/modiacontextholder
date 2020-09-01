package no.nav.sbl.kafka;

import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.rest.domain.RSNyContext;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaUtilTest {

    @Before
    void setUp() {
        System.setProperty("FASIT_ENVIRONMENT_NAME", "T");
    }

    @Test
    void skal_finne_riktig_topic_basert_paa_eventtype() {
        RSNyContext nyAktivEnhetTopic = new RSNyContext().eventType(EventType.NY_AKTIV_ENHET.name());
        String topic = KafkaUtil.asTopic(nyAktivEnhetTopic);
        assertThat(topic).isEqualTo(KafkaUtil.getAktivEnhetTopic());

        RSNyContext nyAktivBrukerContext = new RSNyContext().eventType(EventType.NY_AKTIV_BRUKER.name());
        String nyAktivBrukerTopic = KafkaUtil.asTopic(nyAktivBrukerContext);
        assertThat(nyAktivBrukerTopic).isEqualTo(KafkaUtil.getAktivBrukerTopic());
    }
}
