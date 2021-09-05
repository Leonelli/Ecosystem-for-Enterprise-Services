import json
import os
from datetime import datetime
from flask import Flask, make_response, jsonify
from flask_expects_json import expects_json
import requests

api = Flask(__name__)

#per ogni microservizio una porta diversa

request_schema = {
  "type": "object",
  "properties": {
	"username": { "type": "string" },
	"location": { "type": "string" }
  },
  "required": ["username", "location"]
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

if __name__ == '__main__':
    #api.run()

    # leggere variabili d'ambienete
    #os.environ['service_name'] = "timbrature"
    service_name =os.environ['service_name']
    api.run(host='0.0.0.0', port='5000')
    #scrivere variablili d'ambiente
    # os.environ['Automotive_MNOS'] = '["MNO1","MNO2","MNO3"]'

