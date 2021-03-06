package handlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import email.templates.ArtistResponseTemplate;
import model.Artist;
import model.Journey;
import model.JourneyStage;
import model.User;
import notification.UserNotifier;
import spark.Request;
import spark.Response;
import spark.Route;
import store.InkstepStore;

public class JourneyAcceptHandler implements Route {

  private InkstepStore store;

  public static final DateTimeFormatter STORE_DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static final DateTimeFormatter PARAM_DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public JourneyAcceptHandler(InkstepStore store) {
    this.store = store;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, String> urlParams = bodyParams(request.body());

    int journeyId = Integer.valueOf(request.params(":id"));
    int quoteLower = Integer.valueOf(urlParams.get("quote_lower"));
    int quoteUpper = Integer.valueOf(urlParams.get("quote_upper"));
    String bookingDate = urlParams.get("booking_date");
    String bookingTime = urlParams.get("booking_time");
    String[] time = bookingTime.split("%3A");
    bookingTime = time[0] + ":" + time[1];

    int stage = store.getJourneyStage(journeyId).toCode();

    if (stage == 0) {
      LocalDateTime date =
        LocalDateTime.parse(
          bookingDate + " " + bookingTime, PARAM_DATE_TIME_FORMATTER);

      store.updateQuote(journeyId, quoteLower, quoteUpper);
      store.offerAppointment(journeyId, date.format(STORE_DATE_TIME_FORMATTER));
      store.updateStage(journeyId, JourneyStage.QuoteReceived);

      // Notify the user's device
      Journey j = store.getJourneyFromId(journeyId);
      User u = store.getUserFromID(j.userID);
      Artist a = store.getArtistFromID(j.artistID);
      UserNotifier un = new UserNotifier(u);
      un.notifyStage(a, j, JourneyStage.QuoteReceived);

      return new ArtistResponseTemplate().getTemplate()
        .replace("{{ARTIST NAME}}", a.name)
        .replace("{{USER NAME}}", u.name);
    } else {
      System.out.println("Stage not implemented");
      return Answer.userError("Stage not implemented");
    }
  }

  Map<String, String> bodyParams(String body) {
    Map<String, String> params = new HashMap<>();

    String[] mappings = body.split("&");

    for (String mapping : mappings) {
      String[] valueKey = mapping.split("=");

      params.put(valueKey[0], valueKey[1]);
    }

    return params;
  }
}
