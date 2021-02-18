package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import java.text.MessageFormat;
import javax.swing.JLabel;

public class TemplateLabel extends JLabel {

  private static final long serialVersionUID = -3950596453912717333L;
  public final transient Bindable<TemplateLabel, Object> templateArgumentBindable =
      new Bindable<>(this, TemplateLabel::applyTemplate);

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
