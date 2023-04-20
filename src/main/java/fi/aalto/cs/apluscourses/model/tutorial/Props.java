package fi.aalto.cs.apluscourses.model.tutorial;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Props {
  Props EMPTY = key -> null;

  @Nullable String optProp(@NotNull String key);

  default @NotNull String getProp(@NotNull String key) {
    return Objects.requireNonNull(optProp(key));
  }

  default @NotNull String getProp(@NotNull String key, @NotNull String defaultValue) {
    return Optional.ofNullable(optProp(key)).orElse(defaultValue);
  }

  default <T> @NotNull T parseProp(@NotNull String key,
                                   @NotNull Function<@NotNull String, @NotNull T> parser) {
    return parser.apply(getProp(key));
  }

  @Contract("_, _, null -> null; _, _, !null -> !null")
  default <T> T parseProp(@NotNull String key,
                          @NotNull Function<@NotNull String, @NotNull T> parser,
                          T defaultValue) {
    return Optional.ofNullable(optProp(key)).map(parser).orElse(defaultValue);
  }
}
