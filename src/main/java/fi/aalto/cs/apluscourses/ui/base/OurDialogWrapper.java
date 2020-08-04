package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import fi.aalto.cs.apluscourses.ui.Dialog;
import fi.aalto.cs.apluscourses.ui.utils.ValidationItem;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public abstract class OurDialogWrapper extends DialogWrapper implements Dialog {
  private final List<ValidationItem> validationItems = new ArrayList<>();

  protected OurDialogWrapper(@Nullable Project project) {
    super(project);
  }

  /**
   * Registers a validation item that will be checked when the dialog is validated.
   *
   * @param validationItem A validation item to be registered.
   */
  public void registerValidationItem(ValidationItem validationItem) {
    synchronized (validationItems) {
      validationItems.add(validationItem);
    }
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    synchronized (validationItems) {
      for (ValidationItem validationItem : validationItems) {
        ValidationError validationError = validationItem.validate();
        if (validationError != null) {
          return new ValidationInfo(validationError.getDescription(),
              validationItem.getComponent());
        }
      }
      return null;
    }
  }
}
