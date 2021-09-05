# Build with "docker build --tag <name>:<tag> ." -E.g.-> "docker build --tag paho_client:final ."
FROM python:3.9-alpine


RUN set -eux; \
apk --update add --no-cache nano tzdata build-base libffi-dev openssl-dev; \
#pip install --no-cache-dir cryptography==3.3.2 pyOpenSSL flask flask-expects-json; \
pip install --no-cache-dir flask flask-expects-json requests; \

apk del build-base libffi-dev openssl-dev

RUN cp /usr/share/zoneinfo/Europe/Rome /etc/localtime; \
echo "Europe/Rome" >  /etc/timezone; \
date

COPY stampa_sensibile.py /home/stampa_sensibile.py

RUN chmod +x /home/stampa_sensibile.py

CMD ["python3", "/home/stampa_sensibile.py"]


#docker-compose -f docker-compose.yml up --build --no-start
#docker-compose -f docker-compose.yml up
