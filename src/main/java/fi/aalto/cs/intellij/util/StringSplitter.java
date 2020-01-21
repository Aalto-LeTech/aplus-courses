package fi.aalto.cs.intellij.util;

import org.jetbrains.annotations.NotNull;

/**
 * A class that (hopefully) helps splitting strings.
 * Usage:
 * <pre>
 * {@code
 * StringSplitter s = new StringSplitter("hello-123-world", "-");
 * s.readNext();     // returns "hello"
 * s.readNextInt();  // returns 123
 * s.readNext();     // returns "world"
 * }
 * </pre>
 */
public class StringSplitter {
  private final String string;
  private final char delimiter;
  private final int length;
  private int index = 0;

  /**
   * Constructs a {@link StringSplitter} object that reads {@code string} in parts separated by
   * {@code delimiter}.
   * @param string A string to be split.
   * @param delimiter A character between the parts.
   */
  public StringSplitter(@NotNull String string, char delimiter) {
    this.string = string;
    this.delimiter = delimiter;
    length = string.length();
  }

  /**
   * Reads the next part.
   * @return A string.
   * @throws IllegalStateException Thrown if the end of the string is already reached.
   */
  @NotNull
  public String readNext() throws IllegalStateException {
    if (finished()) {
      throw new IllegalStateException();
    }
    int startIndex = index;
    int endIndex = string.indexOf(delimiter, startIndex);
    if (endIndex == -1) {
      endIndex = length;
    }
    index = endIndex + 1;
    return string.substring(startIndex, endIndex);
  }

  /**
   * Reads the next part and parses it to an integer.
   * @return An integer.
   * @throws IllegalStateException Thrown if the end of the string is already reached.
   * @throws NumberFormatException Thrown if the part cannot be parsed to an integer.
   */
  public int readNextInt() throws IllegalStateException, NumberFormatException {
    return Integer.parseInt(readNext());
  }

  /**
   * Returns a boolean value indicating whether the end of the string is reached.
   * @return True or false.
   */
  public boolean finished() {
    return index > length;
  }
}
