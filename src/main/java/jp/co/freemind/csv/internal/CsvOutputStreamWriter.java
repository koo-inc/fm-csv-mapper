package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jp.co.freemind.csv.CsvFormatter;

public class CsvOutputStreamWriter implements AutoCloseable {
  private final OutputStream os;
  private final Charset charset;
  private final String lineBreak;
  private final boolean bomRequired;
  private boolean firstLine = true;

  public CsvOutputStreamWriter(OutputStream os, CsvFormatter<?> formatter) {
    this(os, formatter.getCharset(), formatter.getLineBreak().getValue(), formatter.isBomRequired());
  }

  public CsvOutputStreamWriter(OutputStream os, Charset charset, String lineBreak, boolean bomRequired) {
    this.os = os;
    this.charset = charset;
    this.lineBreak = lineBreak;
    this.bomRequired = bomRequired;
  }

  public void write(String line) throws IOException {
    if (this.firstLine) {
      this.firstLine = false;
      if (charset.equals(StandardCharsets.UTF_8) && bomRequired) {
        os.write(0xEF);
        os.write(0xBB);
        os.write(0xBF);
      }
    }
    else {
      os.write(lineBreak.getBytes(charset));
    }
    os.write(line.getBytes(charset));
  }

  @Override
  public void close() throws Exception {
    os.close();
  }
}
