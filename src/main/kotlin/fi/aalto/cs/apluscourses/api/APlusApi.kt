package fi.aalto.cs.apluscourses.api

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.model.news.NewsList
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.utils.parser.O1NewsParser
import io.ktor.client.request.forms.*
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.jsoup.Jsoup
import java.time.ZonedDateTime
import kotlin.collections.component1
import kotlin.collections.component2
import fi.aalto.cs.apluscourses.model.Course as CourseModel
import fi.aalto.cs.apluscourses.model.exercise.Exercise as ExerciseModel
import fi.aalto.cs.apluscourses.model.exercise.Submission as SubmissionModel
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult as SubmissionResultModel
import fi.aalto.cs.apluscourses.model.people.Group as GroupModel
import fi.aalto.cs.apluscourses.model.people.User as UserModel

object APlusApi {
    @Resource("/courses/{id}")
    class Course(val id: Long) {
        suspend fun get(project: Project): CourseBody {
            return withContext(Dispatchers.IO) {
                CoursesClient.getInstance(project).getBody<Course, CourseBody>(this@Course)
            }
        }

        @Serializable
        data class CourseBody(
            val htmlUrl: String,
            val endingTime: String,
            val image: String,
        )

        @Resource("exercises")
        class Exercises(val parent: Course) {
            suspend fun get(project: Project): List<CourseModule> {
                return withContext(Dispatchers.IO) {
                    // TODO remove after hasSubmittableFiles fixed
//                    CoursesClient.getInstance(project).getBody<Exercises, CourseModuleResults>(this@Exercises).results
                    val res = CoursesClient.getInstance(project).get<Exercises>(this@Exercises)
                    json.decodeFromString<CourseModuleResults>(
                        res.bodyAsText().replace("\"has_submittable_files\":[]", "\"has_submittable_files\":false")
                    ).results
                }
            }

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
//            val hasSubmittableFiles: Boolean // TODO should always be bool, currently boolean or null or array
                val hasSubmittableFiles: Boolean?
            )

            companion object {
                @OptIn(ExperimentalSerializationApi::class)
                private val json = Json { // TODO remove after hasSubmittableFiles fixed
                    ignoreUnknownKeys = true
                    isLenient = true
                    namingStrategy = JsonNamingStrategy.SnakeCase
                }
            }
        }

        @Resource("points/me")
        class Points(val parent: Course) {
            suspend fun get(project: Project): PointsBody {
                return withContext(Dispatchers.IO) {
                    CoursesClient.getInstance(project).getBody<Points, PointsBody>(this@Points)
                }
            }

            @Serializable
            data class PointsBody(
                val id: Long,
                val url: String,
                val username: String,
                val studentId: String,
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

        @Resource("submissiondata/me")
        class SubmissionData(val parent: Course) {
            suspend fun get(project: Project): List<SubmissionDataBody> {
                val res = withContext(Dispatchers.IO) {
                    CoursesClient.getInstance(project)
                        .get<SubmissionData>(this@SubmissionData) {
                            parameter("best", "no")
                            parameter("format", "json")
                        }
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
                val SubmissionID: Long,
                val UserID: Long,
                val Status: String,
                val Penalty: Double?
            )
        }

        @Resource("news")
        class News(val parent: Course) {
            suspend fun get(project: Project): NewsList {
                val res = withContext(Dispatchers.IO) {
                    CoursesClient.getInstance(project).getBody<News, NewsBody>(this@News)
                }

                return NewsList(res.results.mapNotNull { (id, url, title, _, publishString, language, body, _) ->
                    val courseLanguage = CourseFileManager.getInstance(project).state.language!!
                    if (language != "-" && language != courseLanguage) { // TODO test
                        return@mapNotNull null
                    }

                    val (titleText, bodyText) = if (this.parent.id == 294L) {
                        val titleElement = Jsoup.parseBodyFragment(title).body()
                        val bodyElement = Jsoup.parseBodyFragment(body).body()

                        val parser = O1NewsParser(language) // For O1 2023

                        val titleText = parser.parseTitle(titleElement)
                        val bodyText = parser.parseBody(bodyElement)
                        titleText to bodyText
                    } else {
                        title to body
                    }

                    val publish = ZonedDateTime.parse(publishString)
                    NewsItem(
                        id,
                        titleText,
                        bodyText,
                        publish
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

        @Resource("mygroups")
        class MyGroups(val parent: Course) {
            suspend fun get(project: Project): List<GroupModel> {
                val res = withContext(Dispatchers.IO) {
                    CoursesClient
                        .getInstance(project)
                        .getBody<MyGroups, GroupsBody>(this@MyGroups)
                        .results
                }
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

        suspend fun news(project: Project) = News(this).get(project)
        suspend fun myGroups(project: Project) = MyGroups(this).get(project)

        companion object {
            fun apply(course: Course): Course = Course(course.id.toLong())
        }
    }

    @Resource("/submissions/{id}")
    class Submission(val id: Long) {
        suspend fun get(project: Project): SubmissionBody {
            return withContext(Dispatchers.IO) {
                CoursesClient.getInstance(project).getBody<Submission, SubmissionBody>(this@Submission)
            }
        }

        @Serializable
        data class SubmissionBody(
            val feedback: String,
            val htmlUrl: String,
            val status: String,
            val latePenaltyApplied: Double?,
            val exercise: SubmissionExercise,
        )

        @Serializable
        data class SubmissionExercise(
            val id: Long,
        )
    }

    @Resource("/exercises/{id}")
    class Exercise(val id: Long) {
        suspend fun get(project: Project): Body {
            return withContext(Dispatchers.IO) {
                CoursesClient.getInstance(project).getBody<Exercise, Body>(this@Exercise)
            }
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

        @Resource("submissions")
        class Submissions(val parent: Exercise) {
            @Resource("submit")
            class Submit(val parent: Submissions) {
                suspend fun post(submission: SubmissionModel, project: Project) {
                    val form = formData {
                        append(
                            "__aplus__",
                            "{ \"group\": " + submission.group.id + ", \"lang\": \"" + submission.language + "\" }"
                        )
                        submission.files.forEach { (key, value) ->
                            run {
                                val file = value.toFile()
                                append(key, file.readBytes(), Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                })
                            }
                        }
                    }
                    withContext(Dispatchers.IO) {
                        CoursesClient.getInstance(project).postForm<Submit>(this@Submit, form)
                    }
                }
            }
        }

        suspend fun submit(submission: SubmissionModel, project: Project): Unit =
            Submissions.Submit(Submissions(this)).post(submission, project)
    }

    @Resource("/users")
    class Users {
        @Resource("me")
        class Me(val parent: Users) {
            suspend fun get(project: Project): UserModel? {
                val body = withContext(Dispatchers.IO) {
                    CoursesClient.getInstance(project).getBody<Me, UserBody>(this@Me)
                }
                return UserModel(
                    body.fullName ?: body.username,
                    body.studentId,
                    body.id,
                    body.staffCourses.map { it.id }
                )
            }

            @Serializable
            data class UserBody(
                val username: String,
                val fullName: String?,
                val studentId: String,
                val id: Long,
                val staffCourses: List<Course> = emptyList(),
            )

            @Serializable
            data class Course(
                val id: Long
            )
        }
    }

    fun course(course: CourseModel): Course = Course(course.id.toLong())
    fun exercise(exercise: ExerciseModel): Exercise = Exercise(exercise.id)
    fun submission(submission: SubmissionResultModel): Submission = Submission(submission.id)
    fun me(): Users.Me = Users.Me(Users())

    fun cs(project: Project): CoroutineScope = CoursesClient.getInstance(project).cs
}