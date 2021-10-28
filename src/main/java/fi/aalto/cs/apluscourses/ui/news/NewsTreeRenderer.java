package fi.aalto.cs.apluscourses.ui.news;

import com.intellij.ui.MultilineTreeCellRenderer;
import fi.aalto.cs.apluscourses.model.News;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.news.NewsBodyViewModel;
import fi.aalto.cs.apluscourses.presentation.news.NewsTitleViewModel;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import icons.PluginIcons;
import javax.swing.JTree;

public class NewsTreeRenderer extends MultilineTreeCellRenderer {
  @Override
  protected void initComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
                               boolean hasFocus) {
    SelectableNodeViewModel<?> viewModel = TreeView.getViewModel(value);
    if (viewModel instanceof NewsTitleViewModel) {
      var titleViewModel = (NewsTitleViewModel) viewModel;
      String[] text = {titleViewModel.getPresentableName()};
      setText(text, "");
      setEnabled(true);
      if (((News) viewModel.getModel()).isRead()) {
        setIcon(PluginIcons.A_PLUS_DUMMY);
      } else {
        setIcon(PluginIcons.A_PLUS_INFO);
      }
      setToolTipText("");
    } else if (viewModel instanceof NewsBodyViewModel) {
      var bodyViewModel = (NewsBodyViewModel) viewModel;
      String[] text = {bodyViewModel.getPresentableName()};
      setText(text, "");
      setEnabled(true);
      setIcon(null);
      // TODO add publish time
      setToolTipText("");
    }
  }
}
