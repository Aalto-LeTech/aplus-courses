package fi.aalto.cs.intellij.ui.list;

import java.util.EventListener;

public interface ListActionListener<E> extends EventListener {

  void listActionPerformed(ListActionEvent<E> event);
}
