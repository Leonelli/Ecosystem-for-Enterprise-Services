# Build with "docker build --tag <name>:<tag> ." -E.g.-> "docker build --tag paho_client:final ."
FROM python:3.9


RUN pip install --no-cache-dir pyOpenSSL flask-expects-json requests certifi==2021.5.30 cffi==1.14.6 charset-normalizer==2.0.3 click==8.0.1 cryptography==3.4.7 Flask==2.0.1 idna==3.2 itsdangerous==2.0.1 Jinja2==3.0.1 MarkupSafe==2.0.1 pycparser==2.20 PyJWT==2.1.0 requests==2.26.0 urllib3==1.26.6 Werkzeug==2.0.1; \


RUN cp /usr/share/zoneinfo/Europe/Rome /etc/localtime; \
echo "Europe/Rome" >  /etc/timezone; \
date

COPY sp.py /home/sp.py
COPY jwt_checker.py /home/jwt_checker.py
COPY myfunctions.py /home/myfunctions.py
COPY CIE_CERT_UMB.crt /home/CIE_CERT_UMB.crt

RUN chmod +x /home/sp.py

EXPOSE 5000

CMD ["python3", "/home/sp.py"]


#docker-compose -f docker-compose.yml up --build --no-start
#docker-compose -f docker-compose.yml up
