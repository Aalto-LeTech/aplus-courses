package fi.aalto.cs.apluscourses.presentation

import fi.aalto.cs.apluscourses.model.CourseProject
import fi.aalto.cs.apluscourses.model.User
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel
import fi.aalto.cs.apluscourses.presentation.module.ModuleListViewModel
import fi.aalto.cs.apluscourses.presentation.news.NewsTreeViewModel
import fi.aalto.cs.apluscourses.utils.Event
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Instantiates a class representing the whole main view of the plugin.
 */
class MainViewModel {
    val disposing: Event = Event()

    @JvmField
    val toolWindowCardViewModel: ToolWindowCardViewModel = ToolWindowCardViewModel()

    @JvmField
    val courseViewModel: ObservableProperty<CourseViewModel> = ObservableReadWriteProperty(null)


    @JvmField
    val newsTreeViewModel: ObservableProperty<NewsTreeViewModel?> = ObservableReadWriteProperty(null)

    @JvmField
    val progressViewModel: ProgressViewModel = ProgressViewModel()

    val bannerViewModel: ObservableProperty<BannerViewModel> = ObservableReadWriteProperty(null)

    @JvmField
    var feedbackCss: String? = null

    /**
     * Creates a new [ExercisesTreeViewModel] with the given exercise groups, which is then set
     * to [MainViewModel.exercisesViewModel].
     */
//    fun updateExercisesViewModel(courseProject: CourseProject) {
//        exercisesViewModel.set(
//            ExercisesTreeViewModel.createExerciseTreeViewModel(
//                courseProject.exerciseTree,
//                courseProject
//            )
//        )
//    }

    fun updateCourseViewModel(courseProject: CourseProject) {
        courseViewModel.set(
            CourseViewModel(courseProject.course)
        )
    }

    /**
     * Creates a new [NewsTreeViewModel] from the NewsTree from the [CourseProject],
     * which is then set to [MainViewModel.newsTreeViewModel].
     */
    fun updateNewsViewModel(courseProject: CourseProject) {
        if (courseProject.newsTree == null) {
            newsTreeViewModel.set(null)
        } else {
            newsTreeViewModel.set(NewsTreeViewModel(courseProject.newsTree!!, this))
        }
    }

    fun dispose() {
        disposing.trigger()
    }

//    val exercises: ExercisesTreeViewModel?
//        get() = exercisesViewModel.get()

    val modules: ModuleListViewModel?
        get() = Optional.ofNullable(courseViewModel.get()).map { obj: CourseViewModel -> obj.modules }
            .orElse(null)

    /**
     * Calling this method informs the main view model that the corresponding project has been
     * initialized (by InitializationActivity).
     */
    fun setProjectReady(isReady: Boolean) {
        toolWindowCardViewModel.setProjectReady(isReady)
    }

    /**
     * Sets the ToolWindowCardCViewModel authenticated.
     */
    fun setAuthenticated(authenticated: Boolean) {
        toolWindowCardViewModel.isAuthenticated = authenticated
    }

    fun userChanged(user: User?) {
        setAuthenticated(user != null)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MainViewModel::class.java)
    }
}
