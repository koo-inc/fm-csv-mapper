package jp.co.freemind.csv.internal;

import java.util.function.Function;

/**
 * Created by kakusuke on 15/07/27.
 */
class RunnableUtil {
  @FunctionalInterface
  interface ThrowingRunnable<E extends Throwable> {
    void run() throws E;
    @SuppressWarnings("unchecked")
    default <R extends RuntimeException> Runnable ignoreThrown(Function<E, R> function) {
      return ()-> {
        try {
          run();
        }
        catch (Throwable t) {
          throw function.apply((E) t);
        }
      };
    }
  }
}
