package fi.aalto.cs.apluscourses.presentation.module;

import fi.aalto.cs.apluscourses.presentation.filter.TypedFilter;

public abstract class ModuleFilter extends TypedFilter<ModuleListElementViewModel> {
  protected ModuleFilter() {
    super(ModuleListElementViewModel.class);
  }

  public static class DownloadedFilter extends ModuleFilter {
    @Override
    public boolean applyInternal(ModuleListElementViewModel item) {
      return item.isDownloaded();
    }
  }

  public static class NotDownloadedFilter extends ModuleFilter {
    @Override
    public boolean applyInternal(ModuleListElementViewModel item) {
      return !item.isDownloaded();
    }
  }

}
