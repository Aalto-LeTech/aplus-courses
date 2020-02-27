package fi.aalto.cs.apluscourses.presentation.base;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

public interface SelectableListModel<T> extends ListModel<T> {
  ListSelectionModel getSelectionModel();
}
