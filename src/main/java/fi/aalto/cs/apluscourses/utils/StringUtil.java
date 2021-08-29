package fi.aalto.cs.apluscourses.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jetbrains.annotations.NotNull;

public class StringUtil {

  private StringUtil() {}

  /**
   * Separates the given text into a String[] by tokenizing with the specified delimeter.
   * @param phrase The String to be tokenized.
   * @param delim The delimeter used in the tokenizing.
   * @return The String[] containing the tokens.
   */
  public static String[] getArrayOfTokens(String phrase, char delim) {
    List<String> tokens = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(phrase, String.valueOf(delim));
    while (tokenizer.hasMoreTokens()) {
      tokens.add(tokenizer.nextToken().trim());
    }
    return tokens.toArray(new String[]{});
  }

  public static String strip(@NotNull String string, char character) {
    return stripLeading(stripTrailing(string, character), character);
  }

  /**
   * Removes occurences of a certain character from the beginning of the string.
   *
   * @param string A string.
   * @param character The character to be removed.
   * @return The given string with the leading characters removed.
   */
  public static String stripLeading(@NotNull String string, char character) {
    int length = string.length();
    int index = 0;
    while (index < length && string.charAt(index) == character) {
      index++;
    }
    return string.substring(index);
  }

  /**
   * Removes occurences of a certain character from the end of the string.
   *
   * @param string A string.
   * @param character The character to be removed.
   * @return The given string with the trailing characters removed.
   */
  public static String stripTrailing(@NotNull String string, char character) {
    int index = string.length();
    do {
      index--;
    } while (index >= 0 && string.charAt(index) == character);
    return index < 0 ? "" : string.substring(0, index);
  }
}
