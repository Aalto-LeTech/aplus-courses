package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaClassDeclaration;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;

import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPrimaryConstructor;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass;
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.templates.ScExtendsBlockImpl;

public class ClassDeclarationListener extends ScalaCodeListener {

  private final ScalaClassDeclaration modelScalaClass;

  /**
   * Constructor.
   */
  public ClassDeclarationListener(ListenerCallback callback,
                                  Project project,
                                  String className,
                                  String[] arguments,
                                  String hierarchy,
                                  String[] traitHierarchy,
                                  String typeParameters,
                                  String[] parameterModifiers,
                                  String[] parameterAnnotations,
                                  String fileName) {
    super(callback, project, fileName);
    this.modelScalaClass = new ScalaClassDeclaration(className,arguments, hierarchy,
        traitHierarchy, typeParameters, parameterModifiers, parameterAnnotations);
  }

  /**
   * Creates an instance of ClassDeclarationListener based on the provided arguments.
   */
  public static ClassDeclarationListener create(ListenerCallback callback,
                                                Project project, Arguments arguments) {
    return new ClassDeclarationListener(
      callback, project,
      arguments.getOrThrow("className"),
      arguments.getArrayOrThrow("classArguments"),
      arguments.getOrThrow("classHierarchy"),
      arguments.getArrayOrThrow("traitHierarchy"),
      arguments.getOrThrow("typeParamClause"),
      arguments.getArrayOrThrow("modifiers"),
      arguments.getArrayOrThrow("annotations"),
      arguments.getOrThrow("filePath")
    );
  }

  @Override
  protected void checkPsiFile(@Nullable PsiFile psiFile) {
    if (psiFile == null) {
      return;
    }
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScClass
            && modelScalaClass.checkClassName(((ScClass) element).getName())) {
          PsiElement[] children = element.getChildren();
          Optional<PsiElement> typeParameters = Arrays.stream(children).filter(
              ScTypeParamClause.class::isInstance).findFirst();
          Optional<PsiElement> constructor = Arrays.stream(children).filter(
              ScPrimaryConstructor.class::isInstance).findFirst();
          if (modelScalaClass.checkConstructor(constructor)
              && modelScalaClass.checkTypeParameters(typeParameters)) {
            Optional<PsiElement> extendsBlock = Arrays.stream(children).filter(
                ScExtendsBlockImpl.class::isInstance).findFirst();
            if (modelScalaClass.checkExtendsBlock(extendsBlock)) {
              ApplicationManager.getApplication().invokeLater(callback::callback);
              isCorrect.set(true);
            }
          }
        }
      }
    });
  }

}
