#FROM coreos/apache
FROM ubuntu:14.04
ENV WWW_HOST www.myeuropeana.eu
ENV ANNOTATIONS_HOST test-annotations.europeana.eu
ENV API_HOST appserver:8080
ENV EXHIBITIONS_HOST cd exhibitions-test.cfapps.io
ENV PORTAL_HOST portal-test.cfapps.io
RUN apt-get update \
    	&& apt-get install -y --no-install-recommends \
    		apache2 libapache2-mod-proxy-html libxml2-dev apache2-mpm-itk\
    	&&	a2enmod proxy proxy_http rewrite mpm_prefork
RUN service apache2 restart
COPY htdocs/default/* /var/www/
COPY *.conf /etc/apache2/sites-available/
RUN a2dissite 000-default
#RUN a2ensite myeuropeana.eu.conf
RUN a2ensite 00-www.europeana.eu.conf
RUN service apache2 reload
EXPOSE 80

CMD ["/usr/sbin/apache2ctl", "-D", "FOREGROUND"]