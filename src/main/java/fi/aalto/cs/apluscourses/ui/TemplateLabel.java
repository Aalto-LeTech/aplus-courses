package fi.aalto.cs.apluscourses.ui;

import fi.aalto.cs.apluscourses.utils.bindable.Bindable;
import java.text.MessageFormat;
import javax.swing.JLabel;

public class TemplateLabel extends JLabel {
  public final transient Bindable<TemplateLabel, Object> templateArgumentBindable =
      new Bindable<>(this, TemplateLabel::applyTemplate);
  public final transient Bindable<TemplateLabel, Boolean> isVisibleBindable =
      new Bindable<>(this, TemplateLabel::setVisible);

  private String textTemplate = "{0}";

  public String getTextTemplate() {
    return textTemplate;
  }

  public void setTextTemplate(String textTemplate) {
    this.textTemplate = textTemplate;
  }

  public void applyTemplate(Object value) {
    setText(MessageFormat.format(textTemplate, value));
  }
}
