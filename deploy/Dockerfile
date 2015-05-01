FROM nimmis/java-centos:oracle-8-jdk

RUN mkdir /code
WORKDIR /code

ADD *.jar /code/
ADD createConf.sh /code/

EXPOSE 2551
CMD ["/bin/bash", "createConf.sh"]
