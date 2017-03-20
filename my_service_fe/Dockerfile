FROM python:2.7

MAINTAINER Michael Avrukin "mavrukin@gmail.com"

ADD . /code
WORKDIR /code

EXPOSE 5000

RUN pip install -r requirements.txt
CMD python app.py

# Install Python
# RUN apt-get install -y python-dev python-pip build-essential

# EXPOSE 5000

# COPY . /app
# WORKDIR /app

# RUN pip install -r requirements.txt
# ENTRYPOINT ["python"]
# CMD ["app.py"]



