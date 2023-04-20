package fi.aalto.cs.apluscourses.utils.dom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public abstract class DomParser<T, S> {
  private final @NotNull Map<@NotNull String, @NotNull ObjectParser<T, S>> objectParsers = new HashMap<>();
  private final @NotNull Map<@NotNull String, @NotNull ScopeParser<S>> scopeParsers = new HashMap<>();

  protected @NotNull Stream<T> parse(@NotNull Node rootNode, @NotNull S scope) {
    objectParsers.clear();
    scopeParsers.clear();
    init();
    return parseNode(rootNode, scope);
  }

  protected abstract void init();

  protected void registerObjectParser(@NotNull String key, @NotNull ObjectParser<T, S> objectParser) {
    objectParsers.put(key, objectParser);
  }

  protected void registerScopeParser(@NotNull String key, @NotNull ScopeParser<S> scopeParser) {
    scopeParsers.put(key, scopeParser);
  }

  private @NotNull Stream<@NotNull T> parseChildren(@NotNull Node node, @NotNull S scope) {
    return node.streamChildren().flatMap(child -> parseNode(child, scope));
  }

  private @NotNull Stream<T> parseNode(@NotNull Node node, @NotNull S scope) {
    return parseObject(node, parseScope(node, scope));
  }

  private @NotNull Stream<T> parseObject(@NotNull Node node, @NotNull S scope) {
    String key = node.getKey();
    if (!objectParsers.containsKey(key)) {
      return parseChildren(node, scope);
    }
    var children = parseChildren(node, scope).collect(Collectors.toList());
    return Stream.of(objectParsers.get(key).parseObject(node, children, scope));
  }

  private @NotNull S parseScope(@NotNull Node node, @NotNull S scope) {
    String key = node.getKey();
    return !scopeParsers.containsKey(key) ? scope : scopeParsers.get(key).parseScope(node, scope);
  }

  protected interface ScopeParser<S> {
    @NotNull S parseScope(@NotNull Node node, @NotNull S scope);
  }

  protected interface ObjectParser<T, S> {
    @NotNull T parseObject(@NotNull Node node, @NotNull List<@NotNull T> children, @NotNull S scope);
  }

}
