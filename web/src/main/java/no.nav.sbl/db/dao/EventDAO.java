package no.nav.sbl.db.dao;

import no.nav.sbl.db.domain.PEvent;
import org.hibernate.Criteria;
import org.hibernate.Query;
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

    public Optional<PEvent> sistAktiveBrukerEvent(String veilederIdent, String remoteAdress) {
        Criteria criteria = this
                .getSession()
                .createCriteria(PEvent.class)
                .add(eq("veilederIdent", veilederIdent))
                .add(eq("eventType", NY_AKTIV_BRUKER.name()))
                .add(eq("ip", remoteAdress))
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

    public int slettAlleAvEventType(String eventType) {
        Query query = this.getSession()
                .createQuery("DELETE from PEvent WHERE event_type = :eventType");

        query.setParameter("eventType", eventType);

        return query.executeUpdate();
    }

    public int slettAlleAvEventTypeForVeileder(String eventType, String veilederIdent) {
        Query query = this.getSession()
                .createQuery("DELETE from PEvent WHERE event_type = :eventType AND veileder_ident = :veilederIdent");

        query.setParameter("eventType", eventType);
        query.setParameter("veilederIdent", veilederIdent);

        return query.executeUpdate();
    }

    public void slettAlleEventerUtenomNyeste(List<PEvent> eventer) {
        eventer.stream().sorted((o1, o2) -> o2.getId() < o1.getId() ? 1 : -1)
                .limit(eventer.size() - 1)
                .forEach(pEvent -> this.getSession().delete(pEvent));
    }

    public List<PEvent> hentVeiledersEventerAvType(String eventType, String veilederIdent) {
        return this.getSession()
                .createCriteria(PEvent.class)
                .add(eq("veilederIdent", veilederIdent))
                .add(eq("eventType", eventType))
                .list();
    }

    public List<String> hentUnikeVeilederIdenter() {
        Query query = this.getSession()
                .createQuery("SELECT DISTINCT veilederIdent from PEvent");
        return query.list();
    }

    public int slettAllEventer(String veilederIdent) {
        Query query = this.getSession()
                .createQuery("DELETE from PEvent WHERE veileder_ident = :veilederIdent");

        query.setParameter("veilederIdent", veilederIdent);

        return query.executeUpdate();
    }
}
