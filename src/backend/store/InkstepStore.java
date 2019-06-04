package store;

import java.util.List;

import model.Artist;
import model.Journey;

public interface InkstepStore {
  void addArtist(Artist artist);

  List<Artist> getArtists();

  void putJourneyImages();

  void getJourneysForUsername(String username);

  int createJourney(Journey journey);
}
