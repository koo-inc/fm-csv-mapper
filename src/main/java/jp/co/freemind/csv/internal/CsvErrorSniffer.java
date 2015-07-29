package jp.co.freemind.csv.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jp.co.freemind.csv.Location;

/**
 * Created by kakusuke on 15/07/29.
 */
public class CsvErrorSniffer {
  private Set<Location> locations = new HashSet<>();

  public boolean contains(Location location) {
    return locations.contains(location);
  }

  public boolean hasError() {
    return !locations.isEmpty();
  }

  public Set<Location> getLocations() {
    return Collections.unmodifiableSet(locations);
  }

  void mark(Location location) {
    locations.add(location);
  }
}
