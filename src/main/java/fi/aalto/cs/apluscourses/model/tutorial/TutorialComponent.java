package fi.aalto.cs.apluscourses.model.tutorial;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TutorialComponent {

  default @NotNull CodeContext getCodeContext() {
    throw new UnsupportedOperationException("The component does not provide a code context.");
  }

  @Nullable TutorialComponent getParent();

  default <T> @Nullable T getAncestorOfType(Class<T> clazz) {
    if (clazz.isInstance(this)) {
      return clazz.cast(this);
    }
    var parent = getParent();
    return parent == null ? null : parent.getAncestorOfType(clazz);
  }
}
