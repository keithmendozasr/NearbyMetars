import os
import logging

from google.appengine.dist import use_library
use_library('django', '1.2')

from google.appengine.ext import webapp
from google.appengine.api import users
from google.appengine.ext.webapp import template
import django

templateDir = os.path.join(os.path.dirname(__file__), 'templates')

class BaseHandler(webapp.RequestHandler):
    def __init__(self):
        self.params = {
            'isadmin' : users.is_current_user_admin(),
            'logout_url' : users.create_logout_url('/'),
            'appversion' : "Version %s<br >Django Version: %s<br />%s" % (os.environ['CURRENT_VERSION_ID'], django.get_version(), os.environ['SERVER_SOFTWARE'])
        }
        
        user = users.get_current_user()
        if user is not None:
            self.params['nickname'] = user.nickname()
        
    def displayTemplate(self, templateName):
        #path = os.path.join(os.path.dirname(__file__), 'templates')
        path = os.path.join(templateDir, templateName)
        logging.debug("Value of path: %s" % path)
        self.response.out.write(template.render(path, self.params))
        