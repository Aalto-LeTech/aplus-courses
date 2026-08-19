package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.grading.GradeCalculator
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseSetupStatus
import fi.aalto.cs.apluscourses.services.course.SetupStep
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.InitializationStatus
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import kotlin.time.Clock
import kotlin.time.Instant

enum class Screen {
    NOT_A_COURSE,
    IO_ERROR,
    AUTH,
    NETWORK_ERROR,
    INVALID_TOKEN,
    NOT_ENROLLED,
    MAIN
}

/** The deadline shown under the points table: which week, and when it closes. */
data class ClosingWeek(val number: Int, val closingTime: Instant)

/**
 * Current state of the project shown in the overview.
 */
data class OverviewModel(
    val screen: Screen,
    val configuredCourseName: String? = null,
    val courseName: String? = null,
    val userName: String? = null,
    val bannerUrl: String? = null,
    val coursePageUrl: String? = null,
    val courseHasEnded: Boolean = false,
    val categories: List<String>? = null,
    val collected: Map<String, Int>? = null,
    val maxPoints: Map<String, Int>? = null,
    val grade: String? = null,
    val showsGradeProgression: Boolean = false,
    val pointsUntilNextGrade: Map<String, Int>? = null,
    val setupSteps: List<SetupStep> = emptyList(),
    val maxPointsOfNextGrade: Map<String, Int>? = null,
    val closingWeek: ClosingWeek? = null,
    val weeksKnown: Boolean = false,
    val errorDetails: String? = null
)

fun OverviewModel.showsClosingWeek(): Boolean =
    !courseHasEnded && (closingWeek != null || !weeksKnown)

/**
 * Reads the services into an [OverviewModel], and caches the data.
 */
fun readOverviewModel(project: Project): OverviewModel {
    val courseFiles = CourseFileManager.getInstance(project)
    val configuredCourseName = CourseManager.getInstance(project).state.courseName
    val setup = CourseSetupStatus.getInstance(project)

    if (InitializationStatus.isNotCourse(project)) return OverviewModel(Screen.NOT_A_COURSE)
    if (InitializationStatus.isIoError(project)) {
        return OverviewModel(
            Screen.IO_ERROR,
            errorDetails = CourseManager.getInstance(project).state.networkErrorMessage
        )
    }
    if (CourseManager.authenticated(project) == false) {
        return OverviewModel(Screen.AUTH, configuredCourseName = configuredCourseName)
    }
    CourseManager.error(project)?.let { error ->
        val screen = when (error) {
            CourseManager.Error.NETWORK_ERROR -> Screen.NETWORK_ERROR
            CourseManager.Error.INVALID_TOKEN -> Screen.INVALID_TOKEN
            CourseManager.Error.NOT_ENROLLED -> Screen.NOT_ENROLLED
        }
        return OverviewModel(
            screen,
            configuredCourseName = configuredCourseName,
            errorDetails = CourseManager.getInstance(project).state.networkErrorMessage
                .takeIf { error == CourseManager.Error.NETWORK_ERROR }
        )
    }

    val course = CourseManager.course(project)
    val exercises = ExercisesUpdater.getInstance(project).state
    val grading = CourseManager.getInstance(project).state.grading

    var collected = exercises.userPointsForCategories
        ?.filterKeys { course == null || !course.optionalCategories.contains(it) }

    // Optional categories (challenge, training, ...) are not shown, so the category list has to
    // come from a source that already excludes them.
    var categories = collected?.keys?.sorted()
        ?: grading?.categories()
        ?: courseFiles.state.cachedCategories.takeIf { it.isNotEmpty() }?.sorted()

    var maxPoints = exercises.maxPointsForCategories
        ?: courseFiles.state.cachedCategoryMaxPoints.takeIf { it.isNotEmpty() }

    if (collected != null && collected.isEmpty()) {
        val total = MyBundle.message("ui.OverviewView.totalCategory")
        categories = listOf(total)
        collected = mapOf(total to (exercises.userPointsTotal ?: 0))
        maxPoints = mapOf(total to (exercises.maxPointsTotal ?: 0))
    }
    val gradeData = grading?.let { GradeCalculator.calculate(it, collected ?: emptyMap()) }

    val courseHasEnded =
        if (course?.endingTime != null) CourseManager.isCourseEnded(project)
        else courseFiles.state.courseHadEnded

    course?.name?.let { courseFiles.state.cachedCourseName = it }
    grading?.let { courseFiles.state.showsGradeProgression = it.showsGradeProgression }
    categories?.let { courseFiles.state.cachedCategories = it.toMutableList() }
    maxPoints?.let { max ->
        courseFiles.state.cachedCategoryMaxPoints =
            max.filterKeys { categories == null || categories.contains(it) }.toMutableMap()
    }
    if (course?.endingTime != null) courseFiles.state.courseHadEnded = courseHasEnded
    course?.imageUrl?.let { courseFiles.state.cachedBannerUrl = it }
    course?.htmlUrl?.let { courseFiles.state.cachedCoursePageUrl = it }
    CourseManager.user(project)?.userName?.let { courseFiles.state.cachedUserName = it }

    return OverviewModel(
        screen = Screen.MAIN,
        configuredCourseName = configuredCourseName,
        courseName = course?.name ?: courseFiles.state.cachedCourseName,
        userName = CourseManager.user(project)?.userName ?: courseFiles.state.cachedUserName,
        bannerUrl = course?.imageUrl ?: courseFiles.state.cachedBannerUrl,
        coursePageUrl = course?.htmlUrl ?: courseFiles.state.cachedCoursePageUrl,
        courseHasEnded = courseHasEnded,
        categories = categories,
        collected = collected,
        maxPoints = maxPoints,
        grade = gradeData?.grade,
        showsGradeProgression = grading?.showsGradeProgression ?: courseFiles.state.showsGradeProgression,
        pointsUntilNextGrade = gradeData?.pointsUntilNext,
        setupSteps = setup.visibleSteps.takeIf { !setup.isFinished && setup.isWorthShowing }.orEmpty(),
        maxPointsOfNextGrade = gradeData?.maxOfNext,
        closingWeek = currentWeek(exercises.exerciseGroups.map { it.closingTime }),
        weeksKnown = exercises.exerciseGroups.isNotEmpty()
    )
}

private fun currentWeek(closingTimes: List<String?>): ClosingWeek? {
    val now = Clock.System.now()
    val index = closingTimes.indexOfFirst { it != null && now < Instant.parse(it) }
    val closingTime = closingTimes.getOrNull(index) ?: return null
    return ClosingWeek(number = index + 1, closingTime = Instant.parse(closingTime))
}
