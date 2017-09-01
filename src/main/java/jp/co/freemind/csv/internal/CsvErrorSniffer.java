package jp.co.freemind.csv.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.co.freemind.csv.Location;

/**
 * Created by kakusuke on 15/07/29.
 */
public class CsvErrorSniffer {
  private Map<Location, String> locations = new HashMap<>();

  public boolean contains(Location location) {
    return locations.containsKey(location);
  }

  public boolean hasError() {
    return !locations.isEmpty();
  }

  public Set<Location> getLocations() {
    return Collections.unmodifiableSet(locations.keySet());
  }

  void mark(Location location) {
    mark(location, null);
  }

  void mark(Location location, String message) {
    locations.putIfAbsent(location, message);
  }

  public Map<Location, String> getErrors() {
    return Collections.unmodifiableMap(locations);
  }
}
