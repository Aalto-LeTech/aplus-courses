package fi.aalto.cs.intellij.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;


public class Course {
  private final List<CodeModule> moduleList;
  private final Map<String, CodeModule> moduleIndex;
  private final Object modulesLock = new Object();

  public Course() {
    moduleList = new ArrayList<>();
    moduleIndex = new HashMap<>();
  }

  public void addModule(@NotNull CodeModule module) {
    synchronized (modulesLock) {
      moduleList.add(module);
      moduleIndex.put(module.getName(), module);
    }
  }

  @NotNull
  public CodeModule getModule(@NotNull String moduleName) {
    CodeModule module;
    synchronized (modulesLock) {
      module = moduleIndex.get(moduleName);
    }
    if (module == null) {
      throw new ModuleNotFoundException(moduleName);
    }
    return module;
  }

  @NotNull
  public Iterable<CodeModule> getModuleList() {
    synchronized (modulesLock) {
      return new ArrayList<>(moduleList);
    }
  }

  public static class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException(String moduleName) {
      super("Module '" + moduleName + "' not found.");
    }
  }
}
