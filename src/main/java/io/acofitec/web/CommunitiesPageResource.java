package io.acofitec.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/comunidades")
public class CommunitiesPageResource {

  @Inject
  @Location("comunidades/list.stub")
  Template comunidadesListStub;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance comunidades() {
    return comunidadesListStub.instance();
  }
}
