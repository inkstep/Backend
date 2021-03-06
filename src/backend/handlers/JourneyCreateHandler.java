package handlers;

import static model.JourneyBuilder.aJourney;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import handlers.JourneyCreateHandler.Payload;
import model.Journey;
import model.JourneyBuilder;
import model.JourneyStage;
import model.Validatable;
import store.InkstepStore;

public class JourneyCreateHandler extends AbstractRequestHandler<Payload> {

  private InkstepStore store;

  public JourneyCreateHandler(InkstepStore store) {
    super(Payload.class, store);
    this.store = store;
  }

  @Override protected Answer processImpl(Payload payload, Map<String, String> urlParams) {
    Journey journey = aJourney()
        .withID(-1)
        .withUserID(payload.userID)
        .withArtistID(payload.artistID)
        .withTattooDesc(payload.tattooDesc)
        .withSize(payload.size)
        .withPosition(payload.position)
        .withStyle(payload.style)
        .withAvailability(payload.availability)
        .withNoRefImages(payload.noRefImages)
        .withQuoteLower(-1)
        .withQuoteUpper(-1)
        .withStage(JourneyStage.WaitingQuote.toCode())
        .withBookingDate(null)
        .build();

    int id = store.createJourney(journey);

    Map<String, String> responseMap = new HashMap<String, String>() {{
      put("journey_id", String.valueOf(id));
    }};

    return Answer.ok(dataToJson(responseMap));
  }

  static class Payload implements Validatable {
    public final int userID;
    public final int artistID;

    public final int noRefImages;
    public final String tattooDesc;
    public final String size;
    public final String position;
    public final String availability;
    public final String style;

    @JsonCreator
    public Payload(@JsonProperty("user_id") int userID, @JsonProperty("artist_id") int artistID,
      @JsonProperty("tattoo_desc") String tattooDesc, @JsonProperty("size") String size,
      @JsonProperty("position") String position, @JsonProperty("style") String style,
      @JsonProperty("availability") String availability,
      @JsonProperty("ref_images") int noRefImages) {
      this.userID = userID;
      this.artistID = artistID;
      this.tattooDesc = tattooDesc;
      this.size = size;
      this.position = position;
      this.style = style;
      this.availability = availability;
      this.noRefImages = noRefImages;
    }

    // TODO(DJRHails): Add proper validation for Journey Payload
    @Override public boolean isValid() {
      return userID >= 0 && artistID >= 0 && availability.length() == 7;
    }

    @Override public String toString() {
      return "Journey {" + " userID='" + userID + "'" + ", artistID='" + artistID + "'"
        + ", noRefImages='" + noRefImages + "'" + ", tattooDesc='" + tattooDesc + "'" + ", size='"
        + size + "'" + ", position='" + position + "'" + ", availability='" + availability + "'"
        + ", style='" + style + "'" + "}";
    }
  }
}
