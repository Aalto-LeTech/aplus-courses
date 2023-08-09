package fi.aalto.cs.apluscourses.ui.toolwindowcards;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.ToolWindowCardViewModel;
import java.awt.CardLayout;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class ToolWindowCardView extends JPanel {
  public static final String MAIN_CARD = "MainCard";
  public static final String LOADING_CARD = "LoadingCard";
  public static final String NO_TOKEN_CARD = "NoTokenCard";
  public static final String PROJECT_CARD = "ProjectCard";
  public static final String ERROR_CARD = "ErrorCard";

  @NotNull
  private final CardLayout cl;
  @NotNull
  private final Project project;
  @NotNull
  private final ToolWindowCardViewModel viewModel;
  @NotNull
  private final NoTokenCard noTokenCard;

  /**
   * Constructor.
   */
  public ToolWindowCardView(@NotNull JPanel toolWindowPanel, @NotNull Project project,
                            @NotNull ToolWindowCardViewModel viewModel) {
    viewModel.updated.addListener(this, ToolWindowCardView::viewModelUpdated);

    this.project = project;
    this.viewModel = viewModel;
    this.putClientProperty(ToolWindowCardView.class.getName(), this);
    this.cl = new CardLayout();
    this.setLayout(cl);

    this.add(toolWindowPanel);
    this.cl.addLayoutComponent(toolWindowPanel, MAIN_CARD);

    noTokenCard = new NoTokenCard(project);
    this.add(noTokenCard.getPanel());
    this.cl.addLayoutComponent(noTokenCard.getPanel(), NO_TOKEN_CARD);

    var notAPlusProjectView = new NotAPlusProjectCard();
    this.add(notAPlusProjectView.getPanel());
    this.cl.addLayoutComponent(notAPlusProjectView.getPanel(), PROJECT_CARD);

    var networkErrorView = new NetworkErrorCard(project);
    this.add(networkErrorView.getPanel());
    this.cl.addLayoutComponent(networkErrorView.getPanel(), ERROR_CARD);

    var loadingView = new LoadingCard();
    this.add(loadingView.getPanel());
    this.cl.addLayoutComponent(loadingView.getPanel(), LOADING_CARD);

    cl.show(this, LOADING_CARD);
  }

  /**
   * Called when the view model is updated.
   */
  public void viewModelUpdated() {
    ApplicationManager.getApplication().invokeLater(() -> {
          if (!viewModel.isProjectReady()) {
            cl.show(this, LOADING_CARD);
          } else if (viewModel.isNetworkError()) {
            cl.show(this, ERROR_CARD);
          } else if (!viewModel.isAPlusProject()) {
            cl.show(this, PROJECT_CARD);
          } else if (viewModel.isAuthenticated()) {
            cl.show(this, MAIN_CARD);
          } else {
            noTokenCard.setModulesButtonHidden(viewModel.moduleButtonRequiresLogin());
            cl.show(this, NO_TOKEN_CARD);
          }
        }, ModalityState.any()
    );
  }
}
