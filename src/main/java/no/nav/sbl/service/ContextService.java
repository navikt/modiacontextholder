package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.json.JsonUtils;
import no.nav.sbl.config.FeatureToggle;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.kafka.KafkaUtil;
import no.nav.sbl.mappers.EventMapper;
import no.nav.sbl.redis.Redis;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static no.nav.common.utils.StringUtils.nullOrEmpty;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.mappers.EventMapper.toRSEvent;

@Slf4j
public class ContextService {

    private final EventDAO eventDAO;

    private final KafkaProducer<String, String> kafka;

    private final Redis.Publisher redis;

    private final FeatureToggle featureToggle;

    @Autowired
    public ContextService(EventDAO eventDAO, KafkaProducer<String, String> kafka, FeatureToggle featureToggle) {
        this.eventDAO = eventDAO;
        this.kafka = kafka;
        this.redis = Redis.createPublisher();
        this.featureToggle = featureToggle;
    }

    public RSContext hentVeiledersContext(String veilederIdent) {
        return new RSContext()
                .aktivBruker(hentAktivBruker(veilederIdent).aktivBruker)
                .aktivEnhet(hentAktivEnhet(veilederIdent).aktivEnhet);
    }

    public void oppdaterVeiledersContext(RSNyContext nyContext, String veilederIdent) {
        if (NY_AKTIV_BRUKER.name().equals(nyContext.eventType) && nullOrEmpty(nyContext.verdi)) {
            nullstillAktivBruker(veilederIdent);
            return;
        } else if (nullOrEmpty(nyContext.verdi)) {
            log.warn("Forsøk på å sette aktivEnhet til null, vil generere feil.");
        }

        PEvent event = new PEvent()
                .verdi(nyContext.verdi)
                .eventType(nyContext.eventType)
                .veilederIdent(veilederIdent);

        long id = saveToDb(event);

        if (featureToggle.isKafkaEnabled()) {
            sendToKafka(nyContext, veilederIdent, event.id(id));
        }
    }

    private long saveToDb(PEvent event) {
        return eventDAO.save(event);
    }

    private void sendToKafka(RSNyContext nyContext, String veilederIdent, PEvent event) {
        String topic = KafkaUtil.asTopic(nyContext);
        String eventJson = JsonUtils.toJson(toRSEvent(event));
        kafka.send(new ProducerRecord<>(topic, veilederIdent, eventJson),(metadata, e) -> {
            if (e != null) {
                log.warn("KAFKA SEND FAILED: topic={} offset={} veileder={} message={}", topic, metadata.offset(), veilederIdent, e.getMessage());
            } else {
                log.info("KAFKA SEND OK: topic={} offset={} veileder={} partisjon={}", metadata.topic(), metadata.offset(), veilederIdent, metadata.partition());
            }
        });
        if (featureToggle.isRedisEnabled()) {
            Redis.Message message = Redis.createMessage(topic, veilederIdent, eventJson);
            redis.publishMessage(message);
        }
    }

    public RSContext hentAktivBruker(String veilederIdent) {
        return eventDAO.sistAktiveBrukerEvent(veilederIdent)
                .filter(ContextService::erFortsattAktuell)
                .map(EventMapper::toRSContext)
                .orElse(new RSContext());
    }

    public static boolean erFortsattAktuell(PEvent pEvent) {
        return LocalDate.now().isEqual(pEvent.created.toLocalDate());
    }

    public RSContext hentAktivEnhet(String veilederIdent) {
        return eventDAO.sistAktiveEnhetEvent(veilederIdent)
                .map(EventMapper::toRSContext)
                .orElse(new RSContext());
    }

    public void nullstillContext(String veilederIdent) {
        eventDAO.slettAllEventer(veilederIdent);
    }

    public void nullstillAktivBruker(String veilederIdent) {
        eventDAO.slettAlleAvEventTypeForVeileder(NY_AKTIV_BRUKER.name(), veilederIdent);
    }
}
