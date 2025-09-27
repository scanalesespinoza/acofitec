package io.acofitec.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/sobre")
public class SobreResource {

  @Inject Template sobre;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance sobre() {
    return sobre.data(
        "title",
        "Sobre Acofitec",
        "description",
        "Conoce la misión y visión que guían a la comunidad Acofitec.");
  }
}
