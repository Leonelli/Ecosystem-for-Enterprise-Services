import os
import docker
from flask import Flask, render_template, request

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')


#return render_template("books.html", message=bookid)
@app.route("/receiveAdminData", methods=["POST"])
def adminData():
    id1 = request.form['id1']
    name1 = request.form['name1']
    level1 = request.form['level1']
    try:
        enable1 = request.form.getlist('enable1')
    except:
        enable1 = 0
    id2 = request.form['id2']
    name2 = request.form['name2']
    level2 = request.form['level2']
    try:
        enable2 = request.form.getlist('enable2')
    except:
        enable2 = 0
    id3 = request.form['id3']
    name3 = request.form['name3']
    level3 = request.form['level3']
    try:
        enable3 = request.form.getlist('enable3')
    except:
        enable3 = 0

    id4 = request.form['id4']
    name4 = request.form['name4']
    level4 = request.form['level4']
    try:
        enable4 = request.form.getlist('enable4')
    except:
        enable4 = 0
    id5 = request.form['id5']
    name5 = request.form['name5']
    level5 = request.form['level5']
    try:
        enable5 = request.form.getlist('enable5')
    except:
        enable5 = 0
    id6 = request.form['id6']
    name6 = request.form['name6']
    level6 = request.form['level6']
    try:
        enable6 = request.form.getlist('enable6')
    except:
        enable6 = 0
    id7 = request.form['id7']
    name7 = request.form['name7']
    level7 = request.form['level7']
    try:
        enable7 = request.form.getlist('enable7')
    except:
        enable7 = 0
    id8 = request.form['id8']
    name8 = request.form['name8']
    level8 = request.form['level8']
    try:
        enable8 = request.form.getlist('enable8')
    except:
        enable8 = 0

    f = open("docker-compose.yml", "w", 0o777)
    f.write("version: '3.9'\n")
    f.write("services:\n")

    if enable1 == ['on']:
        f.write("  notifiche:\n")
        f.write("    build: notifiche/.\n")
        f.write("    container_name: notifiche\n")
        f.write("    restart: 'no'\n")
        f.write("    environment:\n")
        f.write("      - service_name=notifiche\n")
        f.write("    depends_on:\n")
        f.write("      service_provider:\n")
        f.write("        condition: service_started\n")

    if enable2 == ['on']:
        #f.write("  stampa:")
        print("stampa")

    if enable3 == ['on']:
        f.write("  stampa_sensibile:\n")
        f.write("    build: stampa_sensibile/.\n")
        f.write("    container_name: stampa_sensibile\n")
        f.write("    restart: 'no'\n")
        f.write("    environment:\n")
        f.write("      - service_name=stampa_sensibile\n")
        f.write("    depends_on:\n")
        f.write("      service_provider:\n")
        f.write("        condition: service_started\n")


    if enable4 == ['on']:
        f.write("  timbrature:\n")
        f.write("    build: timbrature/.\n")
        f.write("    container_name: timbrature\n")
        f.write("    restart: 'no'\n")
        f.write("    environment:\n")
        f.write("      - service_name=timbrature\n")
        f.write("    depends_on:\n")
        f.write("      service_provider:\n")
        f.write("        condition: service_started\n")

    if enable5 == ['on']:
        #f.write("  mensa:")
        print("mensa")

    if enable6 == ['on']:
        #f.write("  parcheggio:")
        print("parcheggio")

    if enable7 == ['on']:
        #f.write("  apertura_uffici:")
        print("apertura_uffici")

    if enable8 == ['on']:
        #f.write("  firma_elettronica:")
        print("firma_elettronica")

    f.write("  service_provider:\n")
    f.write("    build: service_provider/.\n")
    f.write("    container_name: service_provider\n")
    f.write("    restart: 'no'\n")
    f.write("    ports:\n")
    f.write("      - \"5000:5000\"\n")
    f.write("    environment:\n")
    f.write("      - porta=5000\n")

    f.close()
    print("file generated...run file")
    os.system("docker-compose -f docker-compose.yml up --build --no-start")
    os.system("docker-compose -f docker-compose.yml up -d")

    return '''<h1>The Docker/ymk file is created...</h1>
            <ul>
            <li>{}:{}</li>
            <li>{}:{}</li>
            <li>{}:{}</li>
            <li>{}:{}</li>
            <li>{}:{}</li>
            <li>{}:{}</li>
            <li>{}:{}</li>
            <li>{}:{}</li>
            </ul>
            </br>
            <a href='/'>Back</a>
    '''.format(name1,enable1,name2,enable2,name3,enable3,name4,enable4,name5,enable5,name6,enable6,name7,enable7,name8,enable8)

@app.route("/stopServices", methods=["POST"])
def stopServices():
    os.system("docker kill $(docker ps -q)")
    return '''{}... </br> <a href='/'>Back</a>'''.format("stopServices")

@app.route("/statusServices", methods=["POST"])
def statusServices():
    client = docker.from_env()
    print(client.containers.list())
    stringa_ritorno="<h1>Status containers:</h1>"
    stringa_ritorno+="<ul>"
    for container in client.containers.list():
        stringa_ritorno += "<li>"+str(container)[1:-1]+ " - "
        stringa_ritorno += container.attrs['Config']['Image'] + "</li>"
    stringa_ritorno += "</ul>"
    stringa_ritorno+="<a href='/'>Back</a>"
    return stringa_ritorno


if __name__ == '__main__':
    #api.run()
    app.run(host='0.0.0.0', port='5005')

