// `parent` properties are incorrectly marked as unused
@file:Suppress("unused")

package fi.aalto.cs.apluscourses.api

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.model.news.NewsList
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NonNls
import java.time.ZonedDateTime
import fi.aalto.cs.apluscourses.model.Course as CourseModel
import fi.aalto.cs.apluscourses.model.exercise.Exercise as ExerciseModel
import fi.aalto.cs.apluscourses.model.exercise.Submission as SubmissionModel
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult as SubmissionResultModel
import fi.aalto.cs.apluscourses.model.people.Group as GroupModel
import fi.aalto.cs.apluscourses.model.people.User as UserModel

@NonNls
private const val COURSE_PATH = "/courses/{id}"

@NonNls
private const val EXERCISES_PATH = "exercises"

@NonNls
private const val POINTS_PATH = "points/me"

@NonNls
private const val SUBMISSION_DATA_PATH = "submissiondata/me"

@NonNls
private const val NEWS_PATH = "news"

@NonNls
private const val MY_GROUPS_PATH = "mygroups"

@NonNls
private const val SUBMISSION_PATH = "/submissions/{id}"

@NonNls
private const val EXERCISE_PATH = "/exercises/{id}"

@NonNls
private const val SUBMISSIONS_PATH = "submissions"

@NonNls
private const val SUBMIT_PATH = "submit"

@NonNls
private const val USERS_PATH = "/users"

@NonNls
private const val ME_PATH = "me"

@NonNls
private const val PARAM_BEST = "best"

@NonNls
private const val BEST_NO = "no"

@NonNls
private const val PARAM_FORMAT = "format"

@NonNls
private const val FORMAT_JSON = "json"

// The submissiondata columns are in PascalCase.
@NonNls
private const val FIELD_SUBMISSION_ID = "SubmissionID"

@NonNls
private const val FIELD_USER_ID = "UserID"

@NonNls
private const val FIELD_STATUS = "Status"

@NonNls
private const val FIELD_GRADE = "Grade"

@NonNls
private const val FIELD_PENALTY = "Penalty"

/** The multipart field A+ reads the submission's group and language out of. */
@NonNls
private const val APLUS_FORM_FIELD = "__aplus__"

object APlusApi {
    @Resource(COURSE_PATH)
    class Course(val id: Long) {
        suspend fun get(project: Project): CourseBody {
            return CoursesClient.getInstance(project).getBody<Course, CourseBody>(this@Course)
        }

        @Serializable
        data class CourseBody(
            val htmlUrl: String,
            val endingTime: String,
            val image: String?,
        )

        @Resource(EXERCISES_PATH)
        class Exercises(val parent: Course) {
            suspend fun get(project: Project): List<CourseModule> =
                CoursesClient.getInstance(project)
                    .getBody<Exercises, CourseModuleResults>(this@Exercises)
                    .results

            @Serializable
            data class CourseModuleResults(
                val results: List<CourseModule>
            )

            @Serializable
            data class CourseModule(
                val id: Long,
                val url: String,
                val htmlUrl: String,
                val displayName: String,
                val isOpen: Boolean,
                val closingTime: String,
                val exercises: List<Exercise>
            )

            @Serializable
            data class Exercise(
                val id: Long,
                val url: String,
                val htmlUrl: String,
                val displayName: String,
                val maxPoints: Int,
                val maxSubmissions: Int,
                val hierarchicalName: String,
                val difficulty: String,
                @Serializable(with = TruthyBooleanSerializer::class)
                val hasSubmittableFiles: Boolean = false
            )

        }

        @Resource(POINTS_PATH)
        class Points(val parent: Course) {
            suspend fun get(project: Project): PointsBody {
                return CoursesClient.getInstance(project).getBody<Points, PointsBody>(this@Points)
            }

            @Serializable
            data class PointsBody(
                val id: Long,
                val url: String,
                val username: String,
                val studentId: String?,
                val email: String,
                val fullName: String,
                val isExternal: Boolean,
                val tags: List<Tag>,
                val role: String,
                val submissionCount: Int,
                val points: Int,
                val pointsByDifficulty: Map<String, Int>,
                val modules: List<Module>
            )

            @Serializable
            data class Tag(
                val id: Long,
                val url: String,
                val slug: String,
                val name: String
            )

            @Serializable
            data class Module(
                val id: Long,
                val name: String,
                val maxPoints: Int,
                val pointsToPass: Int,
                val submissionCount: Int,
                val points: Int,
                val pointsByDifficulty: Map<String, Int>,
                val passed: Boolean,
                val exercises: List<Exercise>
            )

            @Serializable
            data class Exercise(
                val url: String,
                val bestSubmission: String?,
                val submissions: List<String>,
                val submissionsWithPoints: List<SubmissionWithPoints>,
                val id: Long,
                val name: String,
                val difficulty: String,
                val maxPoints: Int,
                val pointsToPass: Int,
                val submissionCount: Int,
                val points: Int,
                val passed: Boolean,
                val official: Boolean
            )

            @Serializable
            data class SubmissionWithPoints(
                val id: Long,
                val url: String,
                val submissionTime: String,
                val grade: Int
            )
        }

        @Resource(SUBMISSION_DATA_PATH)
        class SubmissionData(val parent: Course) {
            suspend fun get(project: Project): List<SubmissionDataBody> {
                val res = CoursesClient.getInstance(project)
                    .get<SubmissionData>(this@SubmissionData) {
                        parameter(PARAM_BEST, BEST_NO)
                        parameter(PARAM_FORMAT, FORMAT_JSON)
                    }
                return json.decodeFromString<List<SubmissionDataBody>>(res.bodyAsText())
            }

            companion object {
                private val json = Json {
                    ignoreUnknownKeys = true
                }
            }

            @Serializable
            data class SubmissionDataBody(
                @SerialName(FIELD_SUBMISSION_ID) val submissionId: Long,
                @SerialName(FIELD_USER_ID) val userId: Long,
                @SerialName(FIELD_STATUS) val status: String,
                @SerialName(FIELD_GRADE) val grade: Int,
                @SerialName(FIELD_PENALTY) val penalty: Double?
            )
        }

        @Resource(NEWS_PATH)
        class News(private val parent: Course) {
            suspend fun get(project: Project): NewsList {
                val res = CoursesClient.getInstance(project).getBody<News, NewsBody>(this@News)

                val lastNewsReadTime = CourseFileManager.getInstance(project).state.newsReadTime
                return NewsList(res.results.mapNotNull { (id, url, title, _, publishString, language, body, _) ->
                    val courseLanguage = CourseFileManager.getInstance(project).state.language!!
                    if (language != "-" && language != courseLanguage) {
                        return@mapNotNull null
                    }

                    val publish = ZonedDateTime.parse(publishString)
                    val isRead = lastNewsReadTime.epochSeconds >= publish.toEpochSecond()

                    NewsItem(
                        id,
                        title,
                        body,
                        publish,
                        isRead
                    )
                })
            }

            @Serializable
            data class NewsBody(
                val results: List<NewsItemRes>,
            )

            @Serializable
            data class NewsItemRes(
                val id: Long,
                val url: String,
                val title: String,
                val audience: Long,
                val publish: String,
                val language: String,
                val body: String,
                val pin: Boolean,
            )
        }

        @Resource(MY_GROUPS_PATH)
        class MyGroups(val parent: Course) {
            suspend fun get(project: Project): List<GroupModel> {
                val res = CoursesClient
                    .getInstance(project)
                    .getBody<MyGroups, GroupsBody>(this@MyGroups)
                    .results

                return res.map { (id, members) ->
                    GroupModel(
                        id,
                        members.map {
                            GroupModel.GroupMember(
                                // The IDs in the group are different from the user IDs for some reason
                                it.url.substringAfterLast("/").toLong(),
                                it.fullName
                            )
                        })
                }
            }

            @Serializable
            data class GroupsBody(
                val results: List<MyGroup>,
            )

            @Serializable
            data class MyGroup(
                val id: Long,
                val members: List<Member>,
            )

            @Serializable
            data class Member(
                val id: Long,
                val url: String,
                val fullName: String,
            )
        }

        suspend fun points(project: Project): Points.PointsBody =
            Points(this).get(project)

        suspend fun exercises(project: Project): List<Exercises.CourseModule> =
            Exercises(this).get(project)

        suspend fun submissionData(project: Project): List<SubmissionData.SubmissionDataBody> =
            SubmissionData(this).get(project)

        suspend fun news(project: Project): NewsList = News(this).get(project)
        suspend fun myGroups(project: Project): List<GroupModel> = MyGroups(this).get(project)

        companion object {
            fun apply(course: Course): Course = Course(course.id)
        }
    }

    @Resource(SUBMISSION_PATH)
    class Submission(val id: Long) {
        suspend fun get(project: Project): SubmissionBody {
            return CoursesClient.getInstance(project).getBody<Submission, SubmissionBody>(this@Submission)
        }

        @Serializable
        data class SubmissionBody(
            val feedback: String,
            val htmlUrl: String,
            val status: String,
            val grade: Int,
            val latePenaltyApplied: Double?,
            val exercise: SubmissionExercise,
        )

        @Serializable
        data class SubmissionExercise(
            val id: Long,
        )
    }

    @Resource(EXERCISE_PATH)
    class Exercise(val id: Long) {
        suspend fun get(project: Project): Body {
            return CoursesClient.getInstance(project).getBody<Exercise, Body>(this@Exercise)
        }

        @Serializable
        data class Body(
            val exerciseInfo: ExerciseInfo? = null
        )

        @Serializable
        data class ExerciseInfo(
            val formSpec: List<FormSpec>? = null,
            val formI18n: Map<String, Map<String, String>>
        )

        @Serializable
        data class FormSpec(
            val type: String,
            val required: Boolean? = null,
            val title: String,
            val key: String
        )

        @Resource(SUBMISSIONS_PATH)
        class Submissions(val parent: Exercise) {
            @Resource(SUBMIT_PATH)
            class Submit(val parent: Submissions) {
                suspend fun post(submission: SubmissionModel, project: Project) {
                    val form = formData {
                        @NonNls val groupAndLanguage =
                            """{ "group": ${submission.group.id}, "lang": "${submission.language}" }"""
                        append(APLUS_FORM_FIELD, groupAndLanguage)
                        submission.files.forEach { (key, value) ->
                            run {
                                val file = value.toFile()
                                @NonNls val disposition = """filename="${file.name}""""
                                append(key, file.readBytes(), Headers.build {
                                    append(HttpHeaders.ContentDisposition, disposition)
                                })
                            }
                        }
                    }
                    CoursesClient.getInstance(project).postForm<Submit>(this@Submit, form)
                }
            }
        }

        suspend fun submit(submission: SubmissionModel, project: Project): Unit =
            Submissions.Submit(Submissions(this)).post(submission, project)
    }

    @Resource(USERS_PATH)
    class Users {
        @Resource(ME_PATH)
        class Me(val parent: Users) {
            suspend fun get(project: Project): UserModel {
                val body = CoursesClient.getInstance(project).getBody<Me, UserBody>(this@Me)

                return UserModel(
                    body.fullName ?: body.username,
                    body.studentId,
                    body.id,
                    body.enrolledCourses.map { it.id },
                    body.staffCourses.map { it.id }
                )
            }

            @Serializable
            data class UserBody(
                val username: String,
                val fullName: String?,
                val studentId: String?,
                val id: Long,
                val enrolledCourses: List<Course> = emptyList(),
                val staffCourses: List<Course> = emptyList(),
            )

            @Serializable
            data class Course(
                val id: Long
            )
        }
    }

    fun course(course: CourseModel): Course = Course(course.id)
    fun exercise(exercise: ExerciseModel): Exercise = Exercise(exercise.id)
    fun submission(submission: SubmissionResultModel): Submission = Submission(submission.id)
    fun me(): Users.Me = Users.Me(Users())
}