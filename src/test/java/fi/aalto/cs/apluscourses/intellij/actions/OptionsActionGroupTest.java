package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OptionsActionGroupTest {

  AnActionEvent event;
  OptionsActionGroup actionGroup;
  Option filterOption1;
  Option filterOption2;

  /**
   * Called before each test.
   */
  @BeforeEach
  void setUp() {
    Project project = mock(Project.class);
    event = mock(AnActionEvent.class);
    when(event.getProject()).thenReturn(project);

    filterOption1 = new Option("Just some filter", null, item -> Optional.of(true)).init();
    filterOption2 = new Option("Another filter", null, item -> Optional.of(false)).init();

    actionGroup = new OptionsActionGroup() {
      @Override
      public @Nullable Options getOptions(@Nullable Project project) {
        return new Options(filterOption1, filterOption2);
      }
    };
  }

  @Test
  void testGetChildren() {
    AnAction[] children = actionGroup.getChildren(event);

    Assertions.assertTrue(children[0] instanceof OptionAction);
    Assertions.assertTrue(children[1] instanceof OptionAction);
    Assertions.assertTrue(children[2] instanceof Separator);
    Assertions.assertTrue(children[3] instanceof SelectAllOptionsAction);
  }

}
