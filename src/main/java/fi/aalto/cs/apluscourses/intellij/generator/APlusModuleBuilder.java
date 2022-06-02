package fi.aalto.cs.apluscourses.intellij.generator;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

public class APlusModuleBuilder extends ModuleBuilder {
  @Override
  public APlusModuleType getModuleType() {
    return APlusModuleType.getInstance();
  }

  @Override
  public boolean canCreateModule() {
    return false;
  }

  @Override
  public boolean isOpenProjectSettingsAfter() {
    return true;
  }

  @Override
  public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
    return new ModuleWizardStep() {
      @Override
      public JComponent getComponent() {
        // create an empty JPanel so that nothing is shown in the project creation window
        return new JPanel();
      }

      @Override
      public void updateDataModel() {
        // don't do anything
      }
    };
  }
}
