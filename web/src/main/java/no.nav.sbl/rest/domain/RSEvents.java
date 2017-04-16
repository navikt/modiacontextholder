package no.nav.sbl.rest.domain;

import java.util.ArrayList;
import java.util.List;

public class RSEvents {

    public List<RSEvent> events = new ArrayList<>();

    public RSEvents withEvents(List<RSEvent> events) {
        this.events = events;
        return this;
    }
}
