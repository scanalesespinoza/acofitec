package io.acofitec.web;

import io.acofitec.integration.EventCardDTO;
import io.acofitec.integration.EventFlowClient;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/eventos")
public class EventsHubResource {

  private static final Locale LOCALE_ES = Locale.forLanguageTag("es-ES");
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("d 'de' MMM yyyy", LOCALE_ES);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm", LOCALE_ES);

  private final Template template;
  private final EventFlowClient eventFlowClient;
  private final String eventFlowBaseUrl;

  @Inject
  public EventsHubResource(
      @Location("eventos/index") Template template,
      EventFlowClient eventFlowClient,
      @ConfigProperty(name = "eventflow.base-url") String eventFlowBaseUrl) {
    this.template = template;
    this.eventFlowClient = eventFlowClient;
    this.eventFlowBaseUrl = eventFlowBaseUrl;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance eventos() {
    CompletableFuture<List<EventCardDTO>> upcomingFuture =
        CompletableFuture.supplyAsync(eventFlowClient::getUpcoming);
    CompletableFuture<List<EventCardDTO>> pastFuture =
        CompletableFuture.supplyAsync(eventFlowClient::getPast);

    List<EventCardView> upcomingEvents = Collections.emptyList();
    List<EventCardView> pastEvents = Collections.emptyList();
    boolean apiError = false;

    try {
      List<EventCardDTO> upcoming = upcomingFuture.get();
      List<EventCardDTO> past = pastFuture.get();
      upcomingEvents = mapToView(upcoming, true);
      pastEvents = mapToView(past, false);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      apiError = true;
    } catch (ExecutionException e) {
      apiError = true;
    }

    if (apiError) {
      return template.data(
          "upcomingEvents", Collections.emptyList(),
          "pastEvents", Collections.emptyList(),
          "apiError", true,
          "eventFlowBaseUrl", eventFlowBaseUrl);
    }

    return template.data(
        "upcomingEvents", upcomingEvents,
        "pastEvents", pastEvents,
        "apiError", false,
        "eventFlowBaseUrl", eventFlowBaseUrl);
  }

  private List<EventCardView> mapToView(List<EventCardDTO> events, boolean sortAscending) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyList();
    }

    List<EventCardView> mapped = new ArrayList<>(events.size());
    for (EventCardDTO dto : events) {
      mapped.add(new EventCardView(dto, formatDateRange(dto), buildLocation(dto)));
    }

    Comparator<EventCardView> comparator =
        Comparator.comparing(EventCardView::startDateSafe, Comparator.nullsLast(Comparator.naturalOrder()));
    if (!sortAscending) {
      comparator = comparator.reversed();
    }
    mapped.sort(comparator);
    return mapped;
  }

  private String buildLocation(EventCardDTO dto) {
    String city = dto.city == null ? "" : dto.city.trim();
    String country = dto.country == null ? "" : dto.country.trim();
    if (city.isEmpty() && country.isEmpty()) {
      return null;
    }
    if (city.isEmpty()) {
      return country;
    }
    if (country.isEmpty()) {
      return city;
    }
    return city + ", " + country;
  }

  private String formatDateRange(EventCardDTO dto) {
    if (dto == null || dto.startDate == null) {
      return null;
    }

    try {
      OffsetDateTime start = OffsetDateTime.parse(dto.startDate);
      String startDateFormatted = start.format(DATE_FORMATTER);
      String startTimeFormatted = start.format(TIME_FORMATTER);

      if (dto.endDate == null || dto.endDate.isBlank()) {
        return startDateFormatted + " · " + startTimeFormatted;
      }

      OffsetDateTime end = OffsetDateTime.parse(dto.endDate);
      if (start.toLocalDate().equals(end.toLocalDate())) {
        return startDateFormatted + " · " + startTimeFormatted + " - " + end.format(TIME_FORMATTER);
      }
      return startDateFormatted + " - " + end.format(DATE_FORMATTER);
    } catch (DateTimeParseException ex) {
      return dto.startDate;
    }
  }

  public record EventCardView(EventCardDTO event, String displayDate, String location) {

    OffsetDateTime startDateSafe() {
      if (event == null || event.startDate == null) {
        return null;
      }
      try {
        return OffsetDateTime.parse(event.startDate);
      } catch (DateTimeParseException ex) {
        return null;
      }
    }

    public List<String> tagsSafe() {
      return event.tags == null ? List.of() : event.tags;
    }

    public String modeSafe() {
      return event.mode == null ? "" : event.mode;
    }

    public String descriptionSafe() {
      return event.shortDescription == null ? "" : event.shortDescription;
    }

    public String titleSafe() {
      return event.title == null ? "" : event.title;
    }

    public String detailUrlSafe() {
      return event.detailUrl == null ? "#" : event.detailUrl;
    }

    public boolean hasLocation() {
      return location != null && !location.isBlank();
    }

    public String modeLabel() {
      String mode = modeSafe().toLowerCase(LOCALE_ES);
      return switch (mode) {
        case "presencial" -> "Presencial";
        case "remoto" -> "Remoto";
        case "hibrido", "híbrido" -> "Híbrido";
        default -> modeSafe();
      };
    }
  }
}
