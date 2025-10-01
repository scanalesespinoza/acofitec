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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/eventos")
public class EventsHubResource {

  @Inject EventFlowClient eventFlowClient;

  @Inject
  @Location("eventos/index")
  Template eventosIndex;

  @ConfigProperty(name = "eventflow.base-url")
  String eventFlowBaseUrl;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance eventosHub() {
    CompletableFuture<List<EventCardDTO>> upcomingFuture =
        CompletableFuture.supplyAsync(eventFlowClient::getUpcoming);
    CompletableFuture<List<EventCardDTO>> pastFuture =
        CompletableFuture.supplyAsync(eventFlowClient::getPast);

    boolean apiError = false;
    List<EventCardDTO> upcoming = Collections.emptyList();
    List<EventCardDTO> past = Collections.emptyList();

    try {
      upcoming = upcomingFuture.join();
    } catch (RuntimeException ex) {
      apiError = true;
    }

    try {
      past = pastFuture.join();
    } catch (RuntimeException ex) {
      apiError = true;
    }

    List<EventCardView> upcomingViews = mapEvents(upcoming);
    List<EventCardView> pastViews = mapEvents(past);

    return eventosIndex
        .data("upcomingEvents", upcomingViews)
        .data("pastEvents", pastViews)
        .data("apiError", apiError)
        .data("eventFlowPortalUrl", normalizeBaseUrl(eventFlowBaseUrl));
  }

  private List<EventCardView> mapEvents(List<EventCardDTO> events) {
    return events == null ? List.of() : events.stream().map(this::toView).toList();
  }

  private EventCardView toView(EventCardDTO event) {
    String location = formatLocation(event.city, event.country);
    String modeLabel = formatMode(event.mode);
    String formattedDate = formatDateRange(event.startDate, event.endDate);
    List<String> tags =
        event.tags == null ? List.of() : List.copyOf(event.tags);
    String detailUrl =
        event.detailUrl == null || event.detailUrl.isBlank() ? null : event.detailUrl;

    return new EventCardView(
        event.title == null ? "" : event.title,
        event.shortDescription == null ? "" : event.shortDescription,
        location,
        modeLabel,
        formattedDate,
        detailUrl,
        tags);
  }

  private String formatLocation(String city, String country) {
    if ((city == null || city.isBlank()) && (country == null || country.isBlank())) {
      return "";
    }
    if (city == null || city.isBlank()) {
      return country.trim();
    }
    if (country == null || country.isBlank()) {
      return city.trim();
    }
    return city.trim() + ", " + country.trim();
  }

  private String formatMode(String mode) {
    if (mode == null || mode.isBlank()) {
      return "";
    }
    return switch (mode.toLowerCase(Locale.ROOT)) {
      case "presencial" -> "Presencial";
      case "remoto" -> "Remoto";
      case "hibrido", "híbrido" -> "Híbrido";
      default -> mode;
    };
  }

  private String formatDateRange(String startDate, String endDate) {
    OffsetDateTime start = parseDate(startDate);
    OffsetDateTime end = parseDate(endDate);

    if (start == null && end == null) {
      return "Fecha por confirmar";
    }

    if (start != null && end != null) {
      if (start.toLocalDate().equals(end.toLocalDate())) {
        return formatDate(start);
      }
      return formatDate(start) + " — " + formatDate(end);
    }

    return start != null ? formatDate(start) : formatDate(end);
  }

  private OffsetDateTime parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return OffsetDateTime.parse(value);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  private String formatDate(OffsetDateTime date) {
    return date.format(DATE_FORMATTER);
  }

  private String normalizeBaseUrl(String url) {
    if (url == null || url.isBlank()) {
      return "https://eventflow.acofitec.org";
    }
    String value = url.trim();
    while (value.endsWith("/")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("d 'de' MMM yyyy", new Locale("es", "ES"));

  record EventCardView(
      String title,
      String shortDescription,
      String location,
      String mode,
      String formattedDate,
      String detailUrl,
      List<String> tags) {}
}
