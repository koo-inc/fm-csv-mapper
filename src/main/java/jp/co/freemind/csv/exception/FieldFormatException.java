package jp.co.freemind.csv.exception;

import java.util.List;

import jp.co.freemind.csv.Location;
import lombok.Value;

/**
 * Created by kakusuke on 15/07/27.
 */
@Value
public class FieldFormatException extends RuntimeException {
  private final List<Location> locations;
}
