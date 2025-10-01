package io.acofitec.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.QueryParam;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@ApplicationScoped
public class EventFlowClient {

  private final URI baseUri;
  private final Duration timeout;

  public EventFlowClient(
      @ConfigProperty(name = "eventflow.base-url") String baseUrl,
      @ConfigProperty(name = "eventflow.timeout-ms") long timeoutMs) {
    this.baseUri = URI.create(baseUrl);
    this.timeout = Duration.ofMillis(timeoutMs);
  }

  public List<EventCardDTO> getUpcoming() {
    return invoke(api -> api.getEvents("published", "now", null));
  }

  public List<EventCardDTO> getPast() {
    return invoke(api -> api.getEvents("published", null, "now"));
  }

  private List<EventCardDTO> invoke(Function<EventFlowService, List<EventCardDTO>> call) {
    try {
      EventFlowService api =
          RestClientBuilder.newBuilder()
              .baseUri(baseUri)
              .connectTimeout(timeout)
              .readTimeout(timeout)
              .build(EventFlowService.class);
      List<EventCardDTO> response = call.apply(api);
      return response != null ? response : Collections.emptyList();
    } catch (Exception ex) {
      throw new EventFlowClientException("Failed to communicate with EventFlow", ex);
    }
  }

  @Path("/api/events")
  interface EventFlowService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<EventCardDTO> getEvents(
        @QueryParam("status") String status,
        @QueryParam("from") String from,
        @QueryParam("to") String to);
  }
}
