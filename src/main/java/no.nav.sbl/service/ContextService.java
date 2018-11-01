package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.config.FeatureToggle;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.kafka.KafkaUtil;
import no.nav.sbl.mappers.EventMapper;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import java.time.LocalDate;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;

@Slf4j
public class ContextService {

    private final EventDAO eventDAO;

    private final KafkaProducer<String, String> kafka;

    private final FeatureToggle featureToggle;

    @Inject
    public ContextService(EventDAO eventDAO, KafkaProducer<String, String> kafka, FeatureToggle featureToggle) {
        this.eventDAO = eventDAO;
        this.kafka = kafka;
        this.featureToggle = featureToggle;
    }

    public RSContext hentVeiledersContext(String veilederIdent) {
        return new RSContext()
                .aktivBruker(hentAktivBruker(veilederIdent).aktivBruker)
                .aktivEnhet(hentAktivEnhet(veilederIdent).aktivEnhet);
    }

    public void oppdaterVeiledersContext(RSNyContext nyContext, String veilederIdent) {
        saveToDb(nyContext, veilederIdent);

        if (featureToggle.isKafkaEnabled()) {
            sendToKafka(nyContext, veilederIdent);
        }
    }

    private void saveToDb(RSNyContext nyContext, String veilederIdent) {
        eventDAO.save(new PEvent()
                .verdi(nyContext.verdi)
                .eventType(nyContext.eventType)
                .veilederIdent(veilederIdent));
    }

    private void sendToKafka(RSNyContext nyContext, String veilederIdent) {
        String topic = KafkaUtil.asTopic(nyContext);
        String verdi = nyContext.verdi;
        kafka.send(new ProducerRecord<>(topic, veilederIdent, verdi),(metadata, e) -> {
            if (e != null) {
                log.warn("KAFKA SEND FAILED: topic={} offset={} veileder={} message={}", topic, metadata.offset(), veilederIdent, e.getMessage());
            }
            log.info("KAFKA SEND OK: topic={} offset={} veileder={} partisjon={}", metadata.topic(), metadata.offset(), veilederIdent, metadata.partition());
        });
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
