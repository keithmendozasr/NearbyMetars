from google.appengine.ext import db
from google.appengine.api import users
from google.appengine.api import memcache

import os
import sys
import logging

sys.path.append(os.path.join(os.path.abspath(os.path.dirname(__file__)), 'lib'))
import pytz
import datetime
    
class SubmittedRequestInfo(db.Model):
    name = db.StringProperty(required=True)
    email = db.EmailProperty(required=True)
    request = db.CategoryProperty(required=True)
    targetDate = db.DateTimeProperty()
    details = db.TextProperty(required=True)
    requestDate = db.DateTimeProperty(required=True, auto_now_add=True, auto_now=False)
        
class EmailRecepients(db.Model):
    name = db.StringProperty(required=True)
    email = db.EmailProperty(required=True)
    mayLogin = db.BooleanProperty(default=False, indexed = True)
    
emailKey = 'emails'
loginUserKey = 'loginuser'

def getRecepients():
    emails = memcache.get(emailKey)
    if emails is None:
        logging.debug("Need to read emails from storage")
        rslt = EmailRecepients.all()
        
        emails = []
        for item in rslt:
            emails.append(item.email)    
    
        if not memcache.add(emailKey, emails):
            logging.error("Failed to save email recepient to memcache")
    else:
        logging.debug("Emails found in memcache")
        
    logging.debug("Value of emails: %s"%(emails.__str__()))
    return emails

def getLoginUsers():
    loginUsers = memcache.get(loginUserKey)
    if loginUsers is None:
        logging.debug("Need to get login-enabled users from storage")
        rslt = EmailRecepients.gql("WHERE mayLogin = TRUE")
        
        tmp = []
        for item in rslt:
            tmp.append(item.email)
            
        loginUsers = frozenset(tmp)
        if not memcache.add(loginUserKey, loginUsers):
            logging.error("Failed to save login-enabled users to memcache")
    else:
        logging.debug("Login-enabled users found in memcache")
        
    logging.debug("Value of login-enabled users: %s"%(loginUsers.__str__()))
    return loginUsers