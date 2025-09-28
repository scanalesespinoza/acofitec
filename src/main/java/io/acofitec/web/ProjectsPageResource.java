package io.acofitec.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/proyectos")
public class ProjectsPageResource {

  @Inject
  @Location("proyectos/list.stub")
  Template proyectosListStub;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance proyectos() {
    return proyectosListStub.instance();
  }
}
