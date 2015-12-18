package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created by kakusuke on 15/07/23.
 */
public class CsvScanner implements AutoCloseable {
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  private static final char CR = '\r';
  private static final char LF = '\n';
  private static final int CAPACITY = 12;

  private final Reader reader;
  private final Charset charset;
  private final char quoteChar;
  private final char escapeChar;

  private final BufferReader buff = new BufferReader();

  public CsvScanner(InputStream inputStream, Charset charset, char quoteChar, char escapeChar) {
    this.reader = new InputStreamReader(inputStream, charset);
    this.charset = charset;
    this.quoteChar = quoteChar;
    this.escapeChar = escapeChar;
  }

  public String nextLine() throws IOException {
    StringBuilder builder = new StringBuilder(CAPACITY);

    while(true) {
      for (buff.setupToNextChar(); buff.hasNextChar(); buff.prepareToNextChar()) {
        buff.nextChar();

        if (buff.onLeapLineBreak()) continue;

        builder.append(buff.current);

        if (buff.onQuoteChar()) {
          buff.flipQuotedState();
        }
        else if (buff.onLineBreak()) {
          buff.pauseForNextLineReading();

          // 取得しすぎた改行文を取り除いて行を返す
          return builder.substring(0, builder.length() - 1);
        }
      }
      buff.nextBuffer();

      if (buff.isTerminated()) {
        return builder.length() > 0 ? builder.toString() : null;
      }
    }
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  private class BufferReader {
    // ファイル読み込み時からのステータス
    private char[] buffer = new char[CAPACITY];
    private boolean firstReading = true;

    int start = 0;
    int end = 0;
    char current = 0x00;
    char prev = 0x00;

    // nextLine 中のステータス
    int index = 0;
    boolean inEscaped = false;
    boolean inQuoted = false;

    boolean onQuoteChar() {
      return !inEscaped && current == quoteChar;
    }

    boolean onLineBreak() {
      return !inQuoted && (current == CR || current == LF);
    }

    boolean onLeapLineBreak() {
      return !inQuoted && prev == CR && current == LF;
    }

    boolean hasBOM() {
      return charset.equals(UTF_8) && buffer.length > 0 && buffer[0] == '\uFEFF';
    }

    boolean isTerminated() {
      return end < 0;
    }

    boolean hasNextChar() {
      return index < end;
    }

    void setupToNextChar() {
      index = start;
    }

    void nextChar() {
      prev = current;
      current = buffer[index];
    }

    void flipQuotedState() {
      inQuoted = !inQuoted;
    }

    void pauseForNextLineReading() {
      start = index + 1;
    }

    void prepareToNextChar() {
      inEscaped = !inEscaped && current == escapeChar && inQuoted;
      index++;
    }

    void nextBuffer() throws IOException {
      end = reader.read(buffer);

      // BOMつきの場合、最初の１回だけ開始位置を修正
      if (firstReading) {
        firstReading = false;

        start = hasBOM() ? 1 : 0;
      }
      else {
        start = 0;
      }
    }

  }
}
