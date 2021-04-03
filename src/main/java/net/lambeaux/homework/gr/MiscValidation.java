package net.lambeaux.homework.gr;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Holds static validation functions. Not meant to be instantiated. */
public class MiscValidation {

  private static final Logger LOGGER = LoggerFactory.getLogger(MiscValidation.class);

  private MiscValidation() {}

  /**
   * Can be used to assure the caller that {@code cond} is {@code true} and did not throw an
   * exception during evaluation. Otherwise, an {@link IllegalArgumentException} is thrown.
   *
   * @param cond the condition to test.
   * @param errMsg message to use if {@code cond} fails.
   * @throws IllegalArgumentException if {@code cond} is false or threw an exception.
   */
  public static void validateThat(Supplier<Boolean> cond, String errMsg) {
    boolean check;
    try {
      check = cond.get();
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("error executing command, " + errMsg, e);
    }
    if (!check) {
      throw new IllegalArgumentException("error executing command, " + errMsg);
    }
  }

  /**
   * Can be used to assure the caller that the provided {@link Runnable} did not throw a {@link
   * RuntimeException}.
   *
   * @param test the method to invoke.
   * @return {@code true} if {@code test} finished without throwing an exception, {@code false}
   *     otherwise.
   */
  public static boolean noError(Runnable test) {
    try {
      test.run();
      return true;
    } catch (RuntimeException e) {
      LOGGER.trace("noError test failed, returning false", e);
      LOGGER.debug(e.getMessage());
      return false;
    }
  }
}
