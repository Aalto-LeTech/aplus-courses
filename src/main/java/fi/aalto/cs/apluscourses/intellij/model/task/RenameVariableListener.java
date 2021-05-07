package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScVariable;

public class RenameVariableListener implements DocumentListener, ActivitiesListener {

  private final ListenerCallback callback;
  private final String filePath;
  private final String oldName;
  private final String newName;
  private boolean correctVariable;
  private String currentVariable;
  private String elementName;
  private Project project;
  private CaretListener caretListener;
  private Disposable disposable;
  private int line = -1;

  /**
   * Constructor.
   * @param callback the callback for when the task is complete
   * @param filePath The file that contains the variable(s)
   * @param oldName The old name of the variable
   * @param newName The new name of the variable
   * @param project The project where the Tutorial is happening
   */
  public RenameVariableListener(@NotNull ListenerCallback callback,
                                @NotNull String filePath,
                                @NotNull String oldName,
                                @NotNull String newName,
                                Project project) {
    this.project = project;
    this.callback = callback;
    this.filePath = filePath; //TODO establish that the correct file is open! Different issue?
    this.oldName = oldName;
    this.newName = newName;
    caretListener = new CaretListener() {
      @Override
      public void caretPositionChanged(@NotNull CaretEvent event) {
        if (line != event.getNewPosition().line) {
          line = event.getNewPosition().line;
          correctVariable = false;
          currentVariable = "";
        }
      }
    };
  }

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());
    LeafPsiElement element = (LeafPsiElement) psiFile.findElementAt(event.getOffset());
    System.out.println("element: " + element.getText()); //the new value
    elementName = getElementName(element.getText());
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitVariable(ScVariable variable) {
        super.visitVariable(variable);
        if (correctVariable && newName.equals(elementName)) {
          callback.callback();
          return;
        }
        if (!correctVariable && element.getText().trim().isEmpty()) {
          currentVariable = variable.toString().substring(22);
          correctVariable = currentVariable.equals(oldName);
        }
        System.out.println("Found a variable at offset " + variable.toString());
        //gets the variable that is being changed in the form of
        // "ScVariableDefinition: isInTestModeas"

        //handle the case that the caret changes line! -> reset current and correct variable!
        //What would be a use case for that? Is it necessary?
      }
    });

  }

  @Override
  public boolean registerListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    disposable = Disposer.newDisposable();
    multicaster.addCaretListener(caretListener, disposable);
    multicaster.addDocumentListener(this, disposable);
    return false; //TODO check if there is already a var with that name in the file!
  }

  @Override
  public void unregisterListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    multicaster.removeDocumentListener(this);
    multicaster.removeCaretListener(caretListener);
    disposable.dispose();
  }

  private String getElementName(String elementText) {
    if (elementText.contains("IntellijIdeaRulezzz")) {
      elementText = elementText.replaceFirst("IntellijIdeaRulezzz", "");
    }
    return elementText;
  }

  //TODO perhaps a final check make sure that there are no occurencies of the old name
  // (could be with regex)

}

