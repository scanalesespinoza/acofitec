package io.acofitec.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/eventos")
public class EventsPageResource {

  @Inject
  @Location("eventos/list.stub")
  Template eventosListStub;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance eventos() {
    return eventosListStub.instance();
  }
}
