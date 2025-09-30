package io.acofitec.integration;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class EventFlowClient {

  private final EventFlowService eventFlowService;

  public EventFlowClient(
      @ConfigProperty(name = "eventflow.base-url") String baseUrl,
      @ConfigProperty(name = "eventflow.timeout-ms") long timeoutMs) {
    this.eventFlowService =
        QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(baseUrl))
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .readTimeout(Duration.ofMillis(timeoutMs))
            .build(EventFlowService.class);
  }

  public List<EventCardDTO> getUpcoming() {
    List<EventCardDTO> response =
        eventFlowService.getEvents("published", "now", null);
    return response == null ? Collections.emptyList() : response;
  }

  public List<EventCardDTO> getPast() {
    List<EventCardDTO> response =
        eventFlowService.getEvents("published", null, "now");
    return response == null ? Collections.emptyList() : response;
  }

  @Path("/api/events")
  @Produces(MediaType.APPLICATION_JSON)
  private interface EventFlowService {
    @GET
    List<EventCardDTO> getEvents(
        @QueryParam("status") String status,
        @QueryParam("from") String from,
        @QueryParam("to") String to);
  }
}
