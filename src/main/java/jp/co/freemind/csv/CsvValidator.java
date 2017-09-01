package jp.co.freemind.csv;

import jp.co.freemind.csv.exception.ValidationException;

@FunctionalInterface
public interface CsvValidator<T> {
  void validate(T t) throws ValidationException;
}
