package jp.co.freemind.csv;

import lombok.Value;

/**
 * Created by kakusuke on 15/07/27.
 */
@Value public class Location {
  private final int lineNumber;
  private final int columnNumber;
}
