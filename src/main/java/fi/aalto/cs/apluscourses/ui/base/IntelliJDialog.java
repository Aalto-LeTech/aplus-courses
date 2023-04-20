package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.openapi.ui.DialogWrapper;
import fi.aalto.cs.apluscourses.intellij.services.IntelliJContext;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJDialog extends DialogWrapper {

  private final @NotNull Object viewModel;
  private final @NotNull IntelliJContext context;

  public IntelliJDialog(@NotNull Object viewModel, @NotNull IntelliJContext context) {
    super(context.getProject());
    this.viewModel = viewModel;
    this.context = context;
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return context.createViewFor(viewModel);
  }
}
