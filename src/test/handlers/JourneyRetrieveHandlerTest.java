package handlers;

import static model.JourneyBuilder.aJourney;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Journey;
import model.JourneyBuilder;
import org.junit.Test;
import store.InkstepDatabaseStore;
import store.InkstepStore;

public class JourneyRetrieveHandlerTest {

  private final InkstepStore store = mock(InkstepDatabaseStore.class);

  @Test public void jsonEncodedJourneyContainsTheCorrectlyFormattedInfo() {
    int userID = 0;
    int journeyID = 1;
    int artistID = 0;
    int studioID = 0;
    String description = "tattoo.description";
    String size = "tattoo.size";
    String position = "tattoo.position";
    String style = "Blackwork";
    String availability = "0001010";
    int noRefImages = 3;
    int quoteLower = 80;
    int quoteUpper = 100;
    int stage = 0;

    Journey journey =
      aJourney()
      .withID(journeyID)
      .withUserID(artistID)
      .withArtistID(studioID)
      .withTattooDesc(description)
      .withSize(size)
      .withPosition(position)
      .withStyle(style)
      .withAvailability(availability)
      .withNoRefImages(noRefImages)
      .withQuoteLower(quoteLower)
      .withQuoteUpper(quoteUpper)
      .withStage(stage)
      .withBookingDate(null)
      .build();

    List<Journey> journeys = new ArrayList<>();
    journeys.add(journey);

    when(store.getJourneysForUserID(userID)).thenReturn(journeys);
    JourneysRetrieveHandler handler = new JourneysRetrieveHandler(store);

    Map<String, String> params = new HashMap<>();
    params.put("user", String.valueOf(userID));

    String body = handler.process(new EmptyPayload(), params).toString().replace("\"", "");
    System.out.println(body);
    assertThat(body, containsString("size : " + size));
    assertThat(body, containsString("position : " + position));
    assertThat(body, containsString("availability : " + availability));
    assertThat(body, containsString("stage : " + stage));
    assertThat(body, containsString("journeyID : " + journeyID));
    assertThat(body, containsString("userID : " + userID));
    assertThat(body, containsString("artistID : " + artistID));
    assertThat(body, containsString("quoteLower : " + quoteLower));
    assertThat(body, containsString("quoteUpper : " + quoteUpper));
    assertThat(body, containsString("noRefImages : " + noRefImages));
    assertThat(body, containsString("tattooDesc : " + description));
    assertThat(body, containsString("style : " + style));
    assertThat(body, containsString("valid : true"));
  }
}
