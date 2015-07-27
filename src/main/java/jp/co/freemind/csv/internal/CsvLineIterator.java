package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by kakusuke on 15/07/24.
 */
public class CsvLineIterator implements Iterator<String>, AutoCloseable {
  private final CsvScanner scanner;
  private String lastLine;
  private boolean pending = true;

  public CsvLineIterator(CsvScanner scanner) {
    this.scanner = scanner;
  }

  @Override
  public boolean hasNext() {
    if (pending) {
      pending = false;
      lastLine = readLine();
    }

    return lastLine != null;
  }

  @Override
  public String next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    String ret = lastLine;
    lastLine = readLine();
    return ret;
  }

  private String readLine() {
    try {
      return scanner.nextLine();
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws Exception {
    scanner.close();
  }
}
