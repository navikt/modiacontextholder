package no.nav.sbl.kafka;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.EnvironmentUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static no.nav.sbl.config.ApplicationConfig.SRV_PASSWORD_PROPERTY;
import static no.nav.sbl.config.ApplicationConfig.SRV_USERNAME_PROPERTY;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;

@Configuration
@Slf4j
public class KafkaConfig {
    @Bean
    public static KafkaProducer<String, String> kafkaProducer() {
        return new KafkaProducer<>(producerConfigs(true));
    }

    @Bean
    public KafkaHelsesjekk kafkaHelsesjekk(KafkaProducer<String, String> kafkaProducer) {
        return new KafkaHelsesjekk(kafkaProducer);
    }

    @SneakyThrows
    private static Properties producerConfigs(boolean saslSslOn) {
        String username = EnvironmentUtils.getRequiredProperty(SRV_USERNAME_PROPERTY);
        String password = EnvironmentUtils.getRequiredProperty(SRV_PASSWORD_PROPERTY);
        Properties properties = new Properties();
        properties.setProperty("client.id", "veilederflatehendelser.producer");
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        String securityProcotol = saslSslOn ? "SASL_SSL" : "SASL_PLAINEXT";
        properties.setProperty("security.protocol", securityProcotol);

        properties.setProperty("sasl.mechanism", "PLAIN");
        properties.setProperty("bootstrap.servers", KafkaUtil.getKafkaBrokersUrl());
        properties.put(SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username + "\" password=\"" + password + "\";");
        return properties;
    }
}
