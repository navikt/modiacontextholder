package no.nav.sbl.db.dao;

import no.nav.sbl.db.domain.PEvent;
import org.hibernate.Criteria;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_ENHET;
import static org.hibernate.criterion.Order.desc;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.gt;
import static org.slf4j.LoggerFactory.getLogger;

@Transactional
public class EventDAO extends AbstractDAO<PEvent> {

    private static final Logger LOG = getLogger(EventDAO.class);

    @Override
    protected Logger log() {
        return LOG;
    }

    public Optional<PEvent> sistAktiveBrukerEvent(String veilederIdent) {
        Criteria criteria = this
                .getSession()
                .createCriteria(PEvent.class)
                .add(eq("veilederIdent", veilederIdent))
                .add(eq("eventType", NY_AKTIV_BRUKER.name()))
                .addOrder(desc("created"));
        return criteria.list().stream().findFirst();
    }

    public Optional<PEvent> sistAktiveEnhetEvent(String veilederIdent) {
        Criteria criteria = this
                .getSession()
                .createCriteria(PEvent.class)
                .add(eq("veilederIdent", veilederIdent))
                .add(eq("eventType", NY_AKTIV_ENHET.name()))
                .addOrder(desc("created"));
        return criteria.list().stream().findFirst();
    }

    public List<PEvent> finnAlleEventerEtterId(long id) {
        Criteria criteria = this
                .getSession()
                .createCriteria(PEvent.class)
                .add(gt("id", id));
        return criteria.list();
    }
}
