import requests
import jwt_checker

lista_servizi = {'notifiche': 1, 'stampa': 1, 'stampa_sensibile': 3, 'timbrature': 2, 'mensa': 2, 'parcheggio': 1, 'apertura_uffici': 1, 'firma_elettronica': 3}

def level1(user_token):
  print("\nlevel1\n")
  #check only session token
  checkSessionToken(user_token)
  return True

def level2():
  print("\nlevel2\n")
  #check challenge signature TODO: How(?)
  return True

def level3(jwt):
  print("\nlevel3\n")
  if checkJWT(jwt) == 200:
    return True
  else:
    return False

def getUserLevel(token):
  #TODO checking the token from SP API (?)
  return 3

def analyze_token_SP(token):
  # request to SP to get tokenSession
  #check format and if is valid
  url = "https://sp-ipzs-prot.fbk.eu/"
  resp = requests.post(url, data={}, auth=('user', 'pass'))
  print(resp)
  return True

'''
def authentication_check(request):
  if request.method == 'POST':
    response_message = ""
    user_token = request.form.get('user_token')
    jwt = request.form.get('jwt')
    if jwt:
      if jwt_checker.jwt_reader(jwt) == True:
        #response_message = "ACCESSO AL SERVIZIO TRAMITE JWT"
        response_message = 200
      else:
        #response_message = "Wrong JWT"
        response_message = 401
    else:
      if user_token and user_token != "":
        # interrogare SP e inviare credenziali
        if analyze_token_SP(user_token):  # TODO implement the function --> throug SP
          #response_message = "ACCESSO AL SERVIZIO TRAMITE USER_TOKEN"
          response_message = 200
        else:
          #response_message = "Not authorise - Wrong token"
          response_message = 401
      else:
        #response_message = "Not authorise - Missing token - JWT"
        response_message = 403
    return response_message
  else:
    ##response_message = "Error 405 Method Not Allowed"
    response_message = 405
    return response_message
'''

'''
def authentication_check(request):
  if request.method == 'POST':
    response_message = ""
    user_token = request.form.get('user_token')
    jwt = request.form.get('jwt')
    if checkJWT(jwt) != 200:
      return checkSessionToken(user_token)
    else:
      return 200
  else:
    response_message = 405
    return response_message
'''

def authentication_check(request):
  if request.method == 'POST':
    response_message = ""
    user_token = request.form.get('user_token')
    return checkSessionToken(user_token)
  else:
    response_message = 405
    return response_message


def checkJWT(jwt):
    if jwt:
      if jwt_checker.jwt_reader(jwt) == True:
        #response_message = "ACCESSO AL SERVIZIO TRAMITE JWT"
        response_message = 200
      else:
        #response_message = "Wrong JWT"
        response_message = 401
    else:
      ##response_message = "Error 405 Method Not Allowed"
      response_message = 401
    return response_message


def checkSessionToken(user_token):
  if user_token and user_token != "":
    # interrogare SP e inviare credenziali
    if analyze_token_SP(user_token):  # TODO implement the function --> throug SP
      # response_message = "ACCESSO AL SERVIZIO TRAMITE USER_TOKEN"
      response_message = 200
    else:
      # response_message = "Not authorise - Wrong token"
      response_message = 401
  else:
    # response_message = "Not authorise - Missing token - JWT"
    response_message = 403
  return  response_message


def getServiceLevel(servie_name):
  if lista_servizi.get(servie_name):
    return lista_servizi.get(servie_name)
  else:
    return 100 #up to 3 to don't authenticate


def fido():
  return True