FROM python:2.7

MAINTAINER Michael Avrukin "mavrukin@gmail.com"

ADD . /code
WORKDIR /code

EXPOSE 5000

RUN pip install -r requirements.txt
ENTRYPOINT ["python",  "app.py"]

