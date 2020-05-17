package fi.aalto.cs.apluscourses.intellij.model;

import fi.aalto.cs.apluscourses.model.ModelExtensions;
import org.jetbrains.annotations.Nullable;

public class IntelliJModelExtensions {

  private IntelliJModelExtensions() {

  }

  public static class TestComponent extends ModelExtensions.TestComponent
      implements IntelliJComponent<Object> {

    private final Object platformObject;

    public TestComponent(String name, Object platformObject) {
      super(name);
      this.platformObject = platformObject;
    }

    @Nullable
    @Override
    public Object getPlatformObject() {
      return platformObject;
    }
  }
}
