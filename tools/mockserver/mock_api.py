from flask import Flask, json, url_for, send_from_directory, Response
from glob import glob
from pathlib import Path

api = Flask(__name__)

modules = [Path(path).stem for path in glob('modules/*.zip')]
tutorials = [int(Path(path).stem) for path in glob('tutorials/*.json')]

@api.route('/')
def index():
    return 'Hooray, the server is running!'

@api.route('/accounts/accounts/')
def account():
    return 'Your token is: WHATEVER'

@api.route('/config/')
def config():
    conf = {
        'id': '101',
        'name': 'Mock Course',
        'aPlusUrl': url_for('index', _external=True),
        'languages': ['fi', 'en'],
        'resources': {},
        'modules': [{
            'name': module,
            'url': url_for('module_zip', name=module, _external=True),
            'version': '1.0'
        } for module in modules],
        'exerciseModules': {},
        'tutorials': {}
    }
    for tutorial in tutorials:
        with open('tutorials/{}.json'.format(tutorial)) as f:
            conf['tutorials'][str(tutorial)] = {
                'moduleDependencies': [],
                'tasks': json.load(f)
            }
    return json.dumps(conf)

@api.route('/<name>.zip')
def module_zip(name):
    return send_from_directory('modules', name + '.zip')

@api.route('/api/v2/courses/101/exercises/')
def exercises():
    return json.dumps({
        'results': [
            {
                'id': 1,
                'display_name': '|en:Week 1|fi:Kierros 1|',
                'html_url': url_for('week_html', _external=True),
                'is_open': True,
                'exercises': [json.loads(exercise(tutorial)) for tutorial in tutorials]
            },
        ]
    })

@api.route('/api/v2/courses/101/tree/')
def tree():
    return json.dumps({
        'modules': [
            {
                'id': 1,
                'children': [
                    {
                        'children': [
                            { 'id': tutorial }
                            for tutorial in tutorials
                        ]
                    }
                ]
            }
        ]
    })

@api.route('/api/v2/courses/101/')
def course():
    return json.dumps({
        'id': 101,
        'ending_time': '2029-12-31T12:00:00+02:00'
    })

@api.route('/api/v2/courses/101/mygroups/')
def groups():
    return json.dumps({ 'results': [] })

@api.route('/api/v2/courses/101/points/me/')
def points():
    return json.dumps({
        'modules': [
            {
                'id': 1,
                'exercises': [
                    {
                        'id': tutorial,
                        'submissions_with_points': []
                    }
                for tutorial in tutorials]
            }
        ]
    })

@api.route('/api/v2/exercises/<int:id>/')
def exercise(id):
    return json.dumps({
        'id': id,
        'display_name': '|en:Assignment {}|fi:Tehtava {}|'.format(id, id),
        'html_url': url_for('exercise_html', id=id, _external=True),
        'max_points': 1,
        'max_submissions': 1,
        'difficulty': 'A',
        'exercise_info': {
            'form_spec': [
                {
                    'key': 'file1',
                    'type': 'file',
                    'title': 'i18n__ideact_result',
                    'required': True
                }
            ],
            'form_i18n': {
                'i18n__ideact_result': {
                    'en': '_ideact_result',
                    'fi': '_ideact_result'
                }
            }
        },
    })

@api.route('/api/v2/exercises/<int:id>/submissions/submit/', methods=['POST'])
def submit(id):
    resp = Response('{}')
    resp.headers['Location'] = url_for('submission', _external=True)
    return resp

@api.route('/api/v2/users/me/')
def user():
    return json.dumps({ 'username': 'Tester' })

@api.route('/api/v2/submissions/9999/')
def submission():
    return json.dumps({
        "id": 9999,
        "grade": 1,
        "status": "ready",
        "files": []
    })

@api.route('/api/v2/courses/101/students/')
def students():
    return json.dumps({ 'results': [] })

@api.route('/api/v2/courses/101/news/')
def news():
    return json.dumps({ 'results': [] })

@api.route('/exercise_<int:id>/')
def exercise_html(id):
    return "Exercise " + str(id)
@api.route('/exercise_<int:id>/submissions/9999/')
def submission_html(id):
    return 'Submission feedback: ok'

@api.route('/week_1/')
def week_html():
    return "Week 1"

if __name__ == '__main__':
    api.run()
