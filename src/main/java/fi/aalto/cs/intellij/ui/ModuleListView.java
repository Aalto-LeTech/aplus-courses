package fi.aalto.cs.intellij.ui;

import com.intellij.util.NotNullFunction;
import fi.aalto.cs.intellij.presentation.ModuleListPM;
import fi.aalto.cs.intellij.presentation.ModulePM;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ModuleListView extends SelectableListView<ModulePM> {

  public ModuleListView(@NotNull ModuleListPM moduleListPM) {
    super(moduleListPM);
    super.installCellRenderer(ModuleView::new);
  }

  @Override
  public <T> void installCellRenderer(
      @NotNull NotNullFunction<? super T, ? extends JComponent> fun) {
    throw new UnsupportedOperationException("installCellRenderer should not be called.");
  }
}
