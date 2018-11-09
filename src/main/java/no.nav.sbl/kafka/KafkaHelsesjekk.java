package no.nav.sbl.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@Slf4j
public class KafkaHelsesjekk implements Helsesjekk {

    private KafkaProducer<String, String> kafka;

    @Inject
    public KafkaHelsesjekk(KafkaProducer<String, String> kafka) {
        this.kafka = kafka;
    }

    @Override
    public void helsesjekk() {
        kafka.partitionsFor(KafkaUtil.getAktivBrukerTopic());
        kafka.partitionsFor(KafkaUtil.getAktivEnhetTopic());
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata("kafka", KafkaUtil.getKafkaBrokersUrl(), getTopics(), false);
    }

    private String getTopics() {
        return String.format("topics: %s, %s",
                KafkaUtil.getAktivBrukerTopic(),
                KafkaUtil.getAktivEnhetTopic()
        );
    }
}
