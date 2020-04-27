package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.RootPolicy;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NameListRootPolicy extends RootPolicy<List<String>> {

  @Override
  public List<String> visitOrderEntry(@NotNull OrderEntry orderEntry, List<String> value) {
    value.add(orderEntry.getPresentableName());
    return value;
  }
}
