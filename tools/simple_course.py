import csv
import os
import sys
from datetime import timedelta
import django
from django.utils import timezone

def main():
    from django.contrib.auth.models import User
    from course.models import Course, CourseInstance, CourseModule
    from course.models import Enrollment, StudentGroup, LearningObjectCategory
    from exercise.exercise_models import BaseExercise
    from exercise.submission_models import Submission

    now = timezone.now()
    year_later = now + timedelta(days=365)

    user0 = User(id=500)
    user0.username = 'percash0'
    user0.first_name = 'Perry'
    user0.last_name = 'Cash'
    user0.email = 'perry.cash@example.edu'
    user0.set_password('percash0')
    user0.save()

    user1 = User(id=501)
    user1.username = 'zoralst1'
    user1.first_name = 'Zorita'
    user1.last_name = 'Alston'
    user1.email = 'zorita.alston@example.org'
    user1.set_password('zoralst1')
    user1.save()

    user2 = User(id=502)
    user2.username = 'camstei2'
    user2.first_name = 'Cameron'
    user2.last_name = 'Stein'
    user2.email = 'cameron.stein@example.com'
    user2.set_password('camstei2')
    user2.save()

    user3 = User(id=503)
    user3.username = 'brypoll3'
    user3.first_name = 'Brynne'
    user3.last_name = 'Pollard'
    user3.email = 'brynne.pollard@example.net'
    user3.set_password('brypoll3')
    user3.save()

    user4 = User(id=504)
    user4.username = 'allblac4'
    user4.first_name = 'Allistair'
    user4.last_name = 'Blackburn'
    user4.email = 'allistair.blackburn@example.fi'
    user4.set_password('allblac4')
    user4.save()

    user5 = User(id=505)
    user5.username = 'zacbolt5'
    user5.first_name = 'Zachary'
    user5.last_name = 'Bolton'
    user5.email = 'zachary.bolton@example.ee'
    user5.set_password('zacbolt5')
    user5.save()

    user6 = User(id=506)
    user6.username = 'kelwolf6'
    user6.first_name = 'Kelsie'
    user6.last_name = 'Wolf'
    user6.email = 'kelsie.wolf@example.se'
    user6.set_password('kelwolf6')
    user6.save()

    user7 = User(id=507)
    user7.username = 'johmcca7'
    user7.first_name = 'John'
    user7.last_name = 'McCarty'
    user7.email = 'john.mccarty@example.eu'
    user7.set_password('johmcca7')
    user7.save()

    user8 = User(id=508)
    user8.username = 'sherodr8'
    user8.first_name = 'Sheila'
    user8.last_name = 'Rodriquez'
    user8.email = 'sheila.rodriquez@example.ru'
    user8.set_password('sherodr8')
    user8.save()

    user9 = User(id=509)
    user9.username = 'casstan9'
    user9.first_name = 'Cassady'
    user9.last_name = 'Stanley'
    user9.email = 'cassady.stanley@example.no'
    user9.set_password('casstan9')
    user9.save()

    course0 = Course()
    course0.name = 'Test Course'
    course0.url = 'test-course'
    course0.save()

    instance0 = CourseInstance(id=100, course=course0)
    instance0.instance_name = 'Test Instance'
    instance0.url = 'test-instance'
    instance0.starting_time = now
    instance0.ending_time = year_later
    instance0.save()

    Enrollment.objects.create(course_instance=instance0, user_profile=user0.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user1.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user2.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user3.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user4.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user5.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user6.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user7.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user8.userprofile)
    Enrollment.objects.create(course_instance=instance0, user_profile=user9.userprofile)

    group0 = StudentGroup.objects.create(id=200, course_instance=instance0)
    group0.members.add(user0.userprofile)
    group0.members.add(user1.userprofile)
    group0.save()

    group1 = StudentGroup.objects.create(id=201, course_instance=instance0)
    group1.members.add(user0.userprofile)
    group1.members.add(user2.userprofile)
    group1.members.add(user3.userprofile)
    group1.save()

    group2 = StudentGroup.objects.create(id=202, course_instance=instance0)
    group2.members.add(user1.userprofile)
    group2.members.add(user4.userprofile)
    group2.save()

    group3 = StudentGroup.objects.create(id=203, course_instance=instance0)
    group3.members.add(user5.userprofile)
    group3.members.add(user6.userprofile)
    group3.members.add(user7.userprofile)
    group3.members.add(user8.userprofile)
    group3.save()

    module0 = CourseModule(course_instance=instance0)
    module0.name = "First module"
    module0.url = "first-module"
    module0.opening_time = now
    module0.closing_time = year_later
    module0.save()

    module1 = CourseModule(course_instance=instance0)
    module1.name = "Second module"
    module1.url = "second-module"
    module1.opening_time = now
    module1.closing_time = year_later
    module1.save()

    category0 = LearningObjectCategory(course_instance=instance0)
    category0.name = "Some category"
    category0.save()

    exercise0 = BaseExercise(id=300, course_module=module0, category=category0)
    exercise0.name = "Easy exercise"
    exercise0.url = 'easy-exercise'
    exercise0.max_submissions = 10
    exercise0.max_group_size = 4
    exercise0.max_points = 100
    exercise0.points_to_pass = 50
    exercise0.save()

    exercise1 = BaseExercise(id=301, course_module=module0, category=category0)
    exercise1.name = "Hard exercise"
    exercise1.url = 'hard-exercise'
    exercise1.max_submissions = 5
    exercise0.max_group_size = 2
    exercise1.max_points = 100
    exercise1.points_to_pass = 100
    exercise1.save()

    exercise2 = BaseExercise(id=302, course_module=module1, category=category0)
    exercise2.name = "Nice exercise"
    exercise2.url = 'nice-exercise'
    exercise2.max_submissions = 0
    exercise2.max_points = 10
    exercise2.points_to_pass = 0
    exercise2.save()

    submission0 = Submission.objects.create(id=400, exercise=exercise0)
    submission0.submitters.add(user0.userprofile)
    submission0.submitters.add(user1.userprofile)
    submission0.feedback = '<html><body>Not bad.</body></html>'
    submission0.set_points(40, 100)
    submission0.set_ready()
    submission0.save()

    submission1 = Submission.objects.create(id=401, exercise=exercise0)
    submission1.submitters.add(user0.userprofile)
    submission1.submitters.add(user1.userprofile)
    submission1.feedback = '<html><body>Good.</body></html>'
    submission1.set_points(60, 100)
    submission1.set_ready()
    submission1.save()

    submission2 = Submission.objects.create(id=402, exercise=exercise0)
    submission2.submitters.add(user1.userprofile)
    submission2.submitters.add(user4.userprofile)
    submission2.feedback = '<html><body>Good.</body></html>'
    submission2.set_points(50, 100)
    submission2.set_ready()
    submission2.save()

    submission3 = Submission.objects.create(id=403, exercise=exercise2)
    submission3.submitters.add(user0.userprofile)
    submission3.feedback = '<html><body>Excellent.</body></html>'
    submission3.set_points(10, 10)
    submission3.set_ready()
    submission3.save()

if __name__ == '__main__':
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'aplus.settings')
    sys.path.insert(0, '')
    django.setup()
    main()
