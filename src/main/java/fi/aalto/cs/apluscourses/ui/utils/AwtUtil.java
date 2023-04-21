package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.Composite;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;

public class AwtUtil {
  public static final Composite<Component, Container> AWT_COMPOSITE =
      new Composite<>(Container.class, Container::getComponents, Arrays::stream);
}
