package jp.co.freemind.csv.exception;

/**
 * Created by kakusuke on 15/07/28.
 */
public class ReflectiveOperationRuntimeException extends RuntimeException {
  public ReflectiveOperationRuntimeException(ReflectiveOperationException e) {
    super(e);
  }
}
