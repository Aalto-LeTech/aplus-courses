package fi.aalto.cs.apluscourses.presentation.base;

import javax.swing.*;

public interface SelectableListModel<T> extends ListModel<T> {
  ListSelectionModel getSelectionModel();
}
