package no.nav.sbl.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.sbl.config.Pingable;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class KafkaHelsesjekk implements HealthCheck, Pingable {

    private KafkaProducer<String, String> kafka;

    @Autowired
    public KafkaHelsesjekk(KafkaProducer<String, String> kafka) {
        this.kafka = kafka;
    }

    private String getTopics() {
        return String.format("topics: %s, %s",
                KafkaUtil.getAktivBrukerTopic(),
                KafkaUtil.getAktivEnhetTopic()
        );
    }

    @Override
    public HealthCheckResult checkHealth() {
        try {
            kafka.partitionsFor(KafkaUtil.getAktivBrukerTopic());
            kafka.partitionsFor(KafkaUtil.getAktivEnhetTopic());
            return HealthCheckResult.healthy();
        } catch (Exception e) {
            return HealthCheckResult.unhealthy(e);
        }
    }

    @Override
    public SelfTestCheck ping() {
        return new SelfTestCheck(
                "kafka - " + KafkaUtil.getAktivBrukerTopic() + " - " + getTopics(),
                false,
                this::checkHealth
        );
    }
}
