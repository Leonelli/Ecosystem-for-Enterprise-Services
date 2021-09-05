# syntax=docker/dockerfile:1
#FROM python:3.9-alpine

#FROM python:3.9
#WORKDIR /app
#MAINTAINER Matteo Leonelli

#ENV FLASK_APP = sp.py
#ENV FLASK_RUN_HOST=0.0.0.0
#RUN apk add --no-cache gcc musl-dev linux-headers
#COPY app/requirements.txt requirements.txt


#ADD . /app
#RUN mkdir /usr/src/app
#COPY . /usr/src/app/

#COPY requirements.txt requirements.txt
#RUN pip install -r requirements.txt
#COPY . .

#RUN chmod +x /usr/src/app/sp.py

#EXPOSE 5000
#COPY . .
#CMD ["python3", "sp.py"]

#CMD [ "python3", "-m" , "flask", "run"]

#Service provider

FROM python:3.9
ADD . /python-flask
WORKDIR /python-flask
RUN pip install -r requirements.txt
EXPOSE 5000
#COPY . .
CMD ["python3", "/app/app.py"]