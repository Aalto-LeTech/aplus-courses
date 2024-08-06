package fi.aalto.cs.apluscourses.api

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.Course as CourseModel
import fi.aalto.cs.apluscourses.model.temp.Group as GroupModel
import fi.aalto.cs.apluscourses.model.temp.Submission as SubmissionModel
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult as SubmissionResultModel
import fi.aalto.cs.apluscourses.model.exercise.Exercise as ExerciseModel
import fi.aalto.cs.apluscourses.model.people.User as UserModel
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.model.news.NewsTree
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.utils.temp.parser.O1NewsParser
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import java.time.ZonedDateTime
import kotlin.collections.component1
import kotlin.collections.component2

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

        @Resource("news")
        class News(val parent: Course) {
            suspend fun get(project: Project): NewsTree {
                println("news get")
                val res = withContext(Dispatchers.IO) {
                    CoursesClient.getInstance(project).getBody<News, NewsBody>(this@News)
                }
//                println("news res ${res}")

                return NewsTree(res.results.map { (id, url, title, _, publishString, _, body, _) ->
                    val titleElement = Jsoup.parseBodyFragment(title).body()

                    val bodyElement = Jsoup.parseBodyFragment(body).body()

                    val parser = O1NewsParser("en")//NewsParser()
//                val parserKey = null//Optional.ofNullable(course.newsParser).orElse(course.name)
//                parser = when (parserKey) {
//                    O1NewsParser.NAME -> O1NewsParser(language)
//                    else -> NewsParser()
//                }
                    val titleText = parser.parseTitle(titleElement)
                    val bodyText = parser.parseBody(bodyElement)
//                    println(titleText)

                    val publish = ZonedDateTime.parse(publishString)
                    NewsItem(
                        id,
                        url,
                        titleText,
                        bodyText,
                        publish,
//                        ReadNewsImpl()
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
                        members.map { GroupModel.GroupMember(it.id, it.fullName) })
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
                val fullName: String,
            )
        }

        fun news(): News = News(this)
        suspend fun myGroups(project: Project): List<GroupModel> = MyGroups(this).get(project)

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
            val feedback: String
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
            suspend fun get(project: Project): UserModel {
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
                val staffCourses: List<Course>,
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