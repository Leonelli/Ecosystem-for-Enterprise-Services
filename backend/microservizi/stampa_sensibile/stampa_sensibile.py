import json
import os
from datetime import datetime
from flask import Flask, make_response, jsonify
from flask_expects_json import expects_json
import requests

api = Flask(__name__)

#per ogni microservizio una porta diversa

#to modify
request_schema = {
  "type": "object",
  "properties": {
	"username": { "type": "string" },
    "job_id": { "type": "string" },
    "printer_id": { "type": "string" },
    "command": { "type": "string" }
  },
  "required": ["username"]
}



@api.route('/', methods = ['POST'])
@expects_json(request_schema)
def index():
    global service_name
    return make_response(jsonify(f'{service_name} attivo, time: {datetime.now()}'), 200, {"Content-Type": "application/json"})
    #elif result[0] == "C":
    #return make_response(jsonify({'Ok': False, 'Error': 'Not Authorized', 'description': result}), 401,{"Content-Type": "application/json"})
    #else:
    #return make_response(jsonify({'Ok': False, 'Error': 'Server error'}), 500, {"Content-Type": "application/json"})


'''

@api.route('/stampa_sensibile_', methods=['GET', 'POST'])
def stampa_sensibile():
  if authentication_check(request) == 200:
    #set redirection to service
    #return "WELCOME TO PULLPRINTING SERVICE"
    lista_servizi_pullprinting = {'lista_job', 'select_printer', 'print'}
    return str(lista_servizi_pullprinting)
  else:
    return "AUTHENITCATION FAILED"



@api.route('/stampa_sensibile_/list', methods = ['GET', 'POST'])
def jobList():
  if authentication_check(request) == 200:
    job_list = {'job1', 'job2', 'job3'}
    return str(job_list)
  else:
    return "AUTHENITCATION FAILED"
'''


if __name__ == '__main__':
    #api.run()

    # leggere variabili d'ambienete
    #os.environ['service_name'] = "timbrature"
    service_name =os.environ['service_name']
    api.run(host='0.0.0.0', port='5000')
    #scrivere variablili d'ambiente
    # os.environ['Automotive_MNOS'] = '["MNO1","MNO2","MNO3"]'

