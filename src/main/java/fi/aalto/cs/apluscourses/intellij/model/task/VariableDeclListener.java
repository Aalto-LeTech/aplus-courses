package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScConstructorInvocation;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScNewTemplateDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScPatternDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScExtendsBlock;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScTemplateParents;

import java.util.Arrays;
import java.util.Optional;

public class VariableDeclListener extends CodeListener {

  private final String variableName;

  protected VariableDeclListener(ListenerCallback callback, Project project,
                                 String filePath,
                                 String variableName) {
    super(callback, project, filePath);
    this.variableName = variableName;
  }

  public static VariableDeclListener create(ListenerCallback callback,
                                            Project project, Arguments arguments) {
    return new VariableDeclListener(callback, project, arguments.getString("filePath"),
           arguments.getString("variableName"));
  }

  @Override
  protected void checkPsiFile(@NotNull PsiFile psiFile) {
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScPatternDefinition) {
          PsiElement[] children = element.getChildren();
          //TODO get children with different method traversing the siblings!!
          System.out.println("PatternDef" + children[0].getText());
          Optional<PsiElement> templateDef = Arrays.stream(children).filter(
              ScNewTemplateDefinition.class::isInstance).findFirst();
          if (templateDef.isPresent()) {
            children = templateDef.get().getChildren(); //get all the hcildren -> new and ExtendsBlock
            Optional<PsiElement> extendsBlock = Arrays.stream(children).filter(
              ScExtendsBlock.class::isInstance).findFirst();
            if (extendsBlock.isPresent()) { //also check the elements!
              Optional<PsiElement> tempateParents = Arrays.stream(children).filter(
                ScTemplateParents.class::isInstance).findFirst();
              if (tempateParents.isPresent()) {
                Optional<PsiElement> constructorInvocation = Arrays.stream(children).filter(
                ScConstructorInvocation.class::isInstance).findFirst();
                if (constructorInvocation.isPresent()) {
                  children = constructorInvocation.get().getChildren();
                  //SimpleType(MAtch) and argsList
                }
              }
            }
          }
        }
      }
    });
  }
}