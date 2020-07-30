package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import fi.aalto.cs.apluscourses.ui.utils.OneWayBindable;
import java.text.MessageFormat;
import javax.swing.JLabel;

public class TemplateLabel extends JLabel {
  public final transient OneWayBindable<TemplateLabel, Object> templateArgumentBindable =
      new OneWayBindable<>(this, TemplateLabel::applyTemplate, null);

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
