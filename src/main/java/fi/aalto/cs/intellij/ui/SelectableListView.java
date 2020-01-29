package fi.aalto.cs.intellij.ui;

import com.intellij.ui.components.JBList;
import fi.aalto.cs.intellij.presentation.Selectable;
import fi.aalto.cs.intellij.presentation.SelectableListModel;

public class SelectableListView<E extends Selectable> extends JBList<E> {
  public SelectableListView(SelectableListModel<E> listModel) {
    super(listModel);
    setSelectionModel(listModel.getSelectionModel());
  }
}
