package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.editor.Editor;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IntelliJEditorDescendant extends TutorialComponent {
  default @NotNull IntelliJEditor getEditorComponent() {
    return getEditorComponent(this);
  }

  static @NotNull IntelliJEditor getEditorComponent(@Nullable TutorialComponent component) {
    return Objects.requireNonNull(optEditorComponent(component),
        "The component should be enclosed by an editor component.");
  }

  static @Nullable IntelliJEditor optEditorComponent(@Nullable TutorialComponent component) {
    return component == null ? null : component.getAncestorOfType(IntelliJEditor.class);
  }

  default @Nullable Editor getEditor() {
    return getEditorComponent().getEditor();
  }
}
