package io.acofitec.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class HomeResource {

  @Inject Template index;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance home() {
    return index.data(
        "title",
        "Acofitec",
        "description",
        "Impulsamos la innovación con comunidad y colaboración.");
  }
}
