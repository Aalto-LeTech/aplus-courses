package fi.aalto.cs.apluscourses.ui.news;

import static fi.aalto.cs.apluscourses.utils.TreeRendererUtil.isIrrelevantNode;

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
    if (isIrrelevantNode(value)) {
      return;
    }
    SelectableNodeViewModel<?> viewModel = TreeView.getViewModel(value);
    if (viewModel instanceof NewsTitleViewModel titleViewModel) {
      String[] text = {titleViewModel.getPresentableName()};
      setText(text, "");
      setEnabled(true);
      if (((News) viewModel.getModel()).isRead()) {
        setIcon(PluginIcons.A_PLUS_DUMMY);
      } else {
        setIcon(PluginIcons.A_PLUS_INFO);
      }
      setToolTipText(titleViewModel.getModel().getPublishTimeInfo());
    } else if (viewModel instanceof NewsBodyViewModel bodyViewModel) {
      String text = bodyViewModel.getPresentableName();
//      setText(text, "");
      setEnabled(true);
      setIcon(null);
      setToolTipText(bodyViewModel.getModel().getPublishTimeInfo());
    }
  }
}
