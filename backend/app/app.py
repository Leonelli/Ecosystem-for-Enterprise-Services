from flask import Flask, json, jsonify, redirect, url_for
from flask import request
import requests
import jwt_checker
from jwt_checker import *
import myfunctions
from myfunctions import *
import requests

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
  if authentication_check(request) == 200:
    # set redirection to service
    # TODO: show only the services enabled based on SP response (maybe leve)
    return json.dumps(lista_servizi)
  else:
    return "AUTHENITCATION FAILED"


@api.route('/services_list_user/', methods=['POST'])
def getServicesUserList():
  if authentication_check(request) == 200:
    # set redirection to service
    user_level=getUserLevel("")
    # TODO: show only the services enabled based on SP response (maybe leve)
    servizi_abilitati = {}
    for element in lista_servizi:
      if(getUserLevel("") >= lista_servizi[element]):
        servizi_abilitati[element]=lista_servizi[element]
    #return json.dumps(lista_servizi)
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

@api.route('/stampa_sensibile', methods=['GET', 'POST'])
def stampa_sensibile():
  if authentication_check(request) == 200:
    #set redirection to service
    #return "WELCOME TO PULLPRINTING SERVICE"
    lista_servizi_pullprinting = {'lista_job', 'select_printer', 'print'}
    return str(lista_servizi_pullprinting)
  else:
    return "AUTHENITCATION FAILED"



@api.route('/stampa_sensibile/list', methods = ['GET', 'POST'])
def jobList():
  if authentication_check(request) == 200:
    job_list = {'job1', 'job2', 'job3'}
    return str(job_list)
  else:
    return "AUTHENITCATION FAILED"



@api.route('/home', methods = ['GET', 'POST'])
def Home():
  return "HomePage"



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
            return stampa_sensibile()
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



#TODO
#maps 8.A.1/B1/C1 --> ritornare il nome del servizio
#livello 1 -> senza credenziali return L1
##livello 2 -> senza credenziali return challenge
#livello 3 -> senza credenziali return request ID
@api.route('/get_service_level', methods = ['GET', 'POST'])
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
            return stampa_sensibile()
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


if __name__ == '__main__':
    #api.run()
    api.run(host='0.0.0.0', port='5000')

    # fare una get all'SP
    r = requests.post("timbrature:5000", data={'{"username":  "a" ,"location": "a"}'})
    print(r.status_code, r.reason)
    print(r.text)