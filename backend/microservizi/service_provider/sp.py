import datetime
import os
import time

from flask import Flask, json, jsonify, redirect, url_for, make_response
from flask import request
import requests
import jwt_checker
from jwt_checker import *
import myfunctions
from myfunctions import *
import requests
from flask_expects_json import expects_json


#https://realpython.com/token-based-authentication-with-flask/


#lista_servizi = {'notifiche': '1', 'stampa': '1', 'stampa_sensibile': '3', 'timbrature': '2', 'mensa': '1-2', 'parcheggio': '1', 'apertura uffici': '1', 'firma elettronica': '3-firma'}
#print(lista_servizi)

api = Flask(__name__)

url = "http://127.0.0.1:5000"

'''
@api.route('/services/')
def getServices():
  service_name = request.args.get('service_name')
  JWT = request.args.get('jwt')

  str = ""
  if service_name.lower() in lista_servizi:
    print(service_name)
    print(lista_servizi[service_name.lower()])
    print("\nservizio corretto\n")

    str += service_name
    if JWT:
      str += JWT
      if jwt_checker.jwt_reader(JWT) == True:
        print("\nautenticazione corretta\n")
        print(JWT)
        #offer the service if auth successful
        #and provide the level of authentication based on it

        if lista_servizi[service_name.lower()] == 1:
          level1()
        if lista_servizi[service_name.lower()] == 2:
          level2()
        if lista_servizi[service_name.lower()] == 3:
          level3()
        if lista_servizi[service_name.lower()] != 1 and lista_servizi[service_name.lower()] == 2 and lista_servizi[service_name.lower()] == 3:
          return ("SERVICE AUTHENTICATION NOT MATCHED")
        return "accesso al servizio: " + service_name + ", livello richiesto: " + lista_servizi[service_name.lower()]
    else:
      #redirect to authentication request
      #authentication()
      return "No Identification provided"
  else:
    return "No service selected"
  #fido()
  lista_elementi = ""
  for elements in lista_servizi:
    print(elements)
    lista_elementi += elements + "\n"

  #return json.dumps(lista_servizi)
  #return lista_elementi
  return "Authentication failed"
'''

@api.route('/', methods = ['GET', 'POST'])
def index():
  return "INDEX.HTML"



@api.route('/services_list/', methods=['POST'])
def getServicesList():
  data = request.json
  try:
    user_token = data['user_token']
  except:
    user_token = ""
    print("error in user_token")
  if authentication_check(user_token) == 200:
    # set redirection to service
    # TODO: show only the services enabled based on SP response (maybe leve)
    return json.dumps(lista_servizi)
  else:
    return "AUTHENITCATION FAILED"

#3-4-5 msc
@api.route('/services_list_user/', methods=['POST'])
def getServicesUserList():
  data = request.json
  try:
    user_token = data['user_token']
  except:
    user_token = ""
    print("error in user_token")
  if authentication_check(user_token) == 200:
    # set redirection to service
    user_level=getUserLevel("")
    # TODO: show only the services enabled based on SP response (maybe leve)
    servizi_abilitati = {}
    for element in lista_servizi:
      if(getUserLevel("") >= lista_servizi[element]):
        servizi_abilitati[element]=lista_servizi[element]
    #return json.dumps(lista_servizi)
    print("servizi abilitati:"+json.dumps(servizi_abilitati))
    return json.dumps(servizi_abilitati)

  else:
    return "AUTHENITCATION FAILED"


'''
@api.route('/select_service/', methods=['POST'])
def getService():
  if authentication_check(request) == 200:
    service_name = request.form.get('service_name')

    if service_name and service_name.lower() in lista_servizi:
      print(service_name)
      print(lista_servizi[service_name.lower()])
      print("\nservizio corretto\n")

      if lista_servizi[service_name.lower()] == 1:
        level1()
      if lista_servizi[service_name.lower()] == 2:
        level2()
      if lista_servizi[service_name.lower()] == 3:
        level3()
      if lista_servizi[service_name.lower()] != 1 and lista_servizi[service_name.lower()] == 2 and lista_servizi[service_name.lower()] == 3:
        return ("SERVICE AUTHENTICATION NOT MATCHED")
      #return redirect("http://127.0.0.1:5000/"+service_name.lower(), code=302)
      #return redirect(url_for('success', data=request.form.get("data")), code=307)
      data = {
        'jwt': request.form.get('jwt'),
        'user_token': request.form.get('user_token')
      }
      return "accesso al servizio: " + service_name + ", livello richiesto: " + lista_servizi[service_name.lower()]
    else:
      return "No service name provided"
  else:
    return "No service selected"
  return "Authentication failed"
'''


@api.route('/home', methods = ['GET', 'POST'])
def Home():
  return "HomePage"


'''
@api.route('/service_by_name', methods = ['GET', 'POST'])
def getServiceByName():
  if authentication_check(request) == 200:
    service_name = request.form.get('service_name')
    user_token = request.form.get('user_token')
    jwt = request.form.get('jwt')
    print(service_name)
    if service_name and service_name in lista_servizi:
      if getUserLevel(service_name) >= getServiceLevel(service_name):
        check = False
        if getServiceLevel(service_name) == 1:
          check = level1(user_token)
        if getServiceLevel(service_name) == 2:
          check = level2()
        if getServiceLevel(service_name) == 3:
          check = level3(jwt)
        if check == True:
          if service_name == "stampa_sensibile":
            return stampa_sensibile_()
          if service_name == "notifiche":
            return "Notifiche"
          else:
            return "Service not available"
        else:
          return "Livello di autenticazione fallito"
      else:
        return "Livello accesso non sufficiente"
    else:
      return "No service name provided"
  else:
    return "Authentication failed"
'''

#LEVEL 1 and 2(perchè no challenge in the end)
#8.ABC1
#8.ABC2
#def some_function(service_name, a, b, c, d=None, e=None, f=None, g=None, h=None):
@api.route('/service_access_level', methods = ['POST'])
def accessLevel():
  #if token forward directly the request bc level 1
  data = request.json
  try:
    tokenSession = data['tokenSession']
  except:
    tokenSession = ""
    print("error in tokenSession")
  try:
    service_name = data['service_name']
  except:
    service_name = ""
    print("error in service name")

  #TODO service_name.lower() to avoid capital errors

  if tokenSession and myfunctions.getServiceLevel(service_name)==1:
    return requestWithToken()
  else:
    if service_name:
      if service_name in lista_servizi:
        livello_servizio = myfunctions.getServiceLevel(service_name)
        if livello_servizio == 1:
          return make_response(str(myfunctions.getServiceLevel(service_name)), 200)
        elif livello_servizio == 2:
          return make_response(str(myfunctions.generateChallenge()), 200)
        elif livello_servizio == 3:
          return make_response(str(myfunctions.generateRequestID()), 200)
        else:
          return make_response("service level not managed", 404)
      else:
        return make_response("service not implemented", 400)
    else:
      return make_response("missing argument: service_name", 400)


#da 8.3 in poi ABC
@api.route('/forward_request', methods = ['POST'])
def requestWithToken():
  print(request.json,flush=True)
  data = request.json
  try:
    tokenSession = data['tokenSession']
  except:
    tokenSession=""
    print("error in tokenSession")
  try:
    jwt = data['jwt']
  except:
    jwt=""
    print("error in jwt")
  try:
    service_name = data['service_name']
  except:
    service_name=""
    print("error in service name")
  if service_name and service_name in lista_servizi:
    livello_servizio = myfunctions.getServiceLevel(service_name)
    if myfunctions.getUserLevel(service_name) >= myfunctions.getServiceLevel(service_name):
      if livello_servizio == 1 or livello_servizio == 2:
        # accesso con token
        if tokenSession:
          if myfunctions.checkSessionToken(tokenSession) == 200:
            return generic_redirect(service_name)
            '''
            if service_name == "stampa_sensibile":
              return stampa_sensibile_()
            elif service_name == "notifiche":
              return notifiche()
            elif service_name == "timbrature":
              return timbrature()
            else:
              return make_response("Service not available")
            '''
          else:
            return make_response("Wrong token")
        else:
          return make_response("Missing token")
      elif livello_servizio == 3:
        # accesso con jwt
        if jwt:
          if level3(jwt):
            return generic_redirect(service_name)
            '''
            if service_name == "stampa_sensibile":
              return stampa_sensibile_()
            elif service_name == "notifiche":
              return notifiche()
            elif service_name == "timbrature":
              return timbrature()
            else:
              return make_response("Service not available")
            '''
          else:
            return make_response("Wrong jwt")
        else:
          return make_response("Missing jwt")
    else:
      return "User permission level not macthed with service levle"
  else:
    return "Service not available"


#maps 8.A.1/B1/C1 --> ritornare il nome del servizio
#livello 1 -> senza credenziali return L1
##livello 2 -> senza credenziali return challenge
#livello 3 -> senza credenziali return request ID
'''
@api.route('/get_service_level', methods = ['POST'])
def getServiceLevel():
  if authentication_check(request) == 200:
    service_name = request.form.get('service_name')
    user_token = request.form.get('user_token')
    jwt = request.form.get('jwt')
    print(service_name)
    if service_name and service_name in lista_servizi:
      if getUserLevel(service_name) >= getServiceLevel(service_name):
        check = False
        if getServiceLevel(service_name) == 1:
          check = level1(user_token)
        if getServiceLevel(service_name) == 2:
          check = level2()
        if getServiceLevel(service_name) == 3:
          check = level3(jwt)
        if check == True:
          if service_name == "stampa_sensibile":
            return stampa_sensibile_()
          elif service_name == "notifiche":
            return notifiche()
          elif service_name == "timbrature":
            return timbrature()
          else:
            return "Service not available"
        else:
          return "Livello di autenticazione fallito"
      else:
        return "Livello accesso non sufficiente"
    else:
      return "No service name provided"
  else:
    return "Authentication failed"
'''



@api.route('/test/<query>', methods=['GET', 'POST'])
def generic_redirect(query):
  # fare una get all'SP
  req_data = request.get_json()
  #req_data={'{"username":  "a" ,"location": "a"}'}
  print(req_data, flush=True)
  r = requests.post("http://"+query+":5000", json=req_data)
  print(r.status_code, r.reason,flush=True)
  print(r.text, flush=True)
  return make_response(r.text, r.status_code)

'''

#come fare endpoint dinamico basato su docker container
#https://www.google.com/url?q=https://pythonise.com/series/learning-flask/generating-dynamic-urls-with-flask&sa=D&source=hangouts&ust=1628868695058000&usg=AFQjCNGBLH0uD5egE0hoSeamR_feyzaT2A
@api.route('/timbrature', methods=['GET', 'POST'])
def timbrature():
  # fare una get all'SP
  req_data = request.get_json()
  #req_data={'{"username":  "a" ,"location": "a"}'}
  print(req_data, flush=True)
  r = requests.post("http://timbrature:5000", json=req_data)
  print(r.status_code, r.reason,flush=True)
  print(r.text, flush=True)
  return make_response(r.text, r.status_code)



#redirect services
#automatic dinamic redirect
@api.route('/notifiche', methods=['GET', 'POST'])
def notifiche():
  # fare una get all'SP
  req_data = request.get_json()
  #req_data={'{"username":  "a" ,"location": "a"}'}
  print(req_data,flush=True)
  r = requests.post("http://notifiche:5000", json=req_data)
  print(r.status_code, r.reason,flush=True)
  print(r.text,flush=True)
  return make_response(r.text, r.status_code)

@api.route('/stampa_sensibile', methods=['GET', 'POST'])
def stampa_sensibile_():
  # fare una get all'SP
  req_data = request.get_json()
  #req_data={'{"username":  "a" ,"location": "a"}'}
  print(req_data,flush=True)
  r = requests.post("http://stampa_sensibile:5000", json=req_data)
  print(r.status_code, r.reason,flush=True)
  print(r.text,flush=True)
  return make_response(r.text, r.status_code)
'''

if __name__ == '__main__':
    #api.run()
    _port =os.environ['porta']
    api.run(host='0.0.0.0', port=_port)
