package no.nav.sbl.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.status;
import static no.nav.metrics.MetricsFactory.createEvent;

@Provider
public class RuntimeLogger implements ExceptionMapper<RuntimeException> {
    @Override
    public Response toResponse(RuntimeException e) {
        if (statuskode(e) == 500) {
            createEvent("runtimeexception").report();
        }
        return status(statuskode(e)).build();
    }

    private int statuskode(RuntimeException e) {
        if (e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse().getStatus();
        } else {
            return 500;
        }
    }
}
