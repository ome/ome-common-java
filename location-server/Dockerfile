FROM minio/minio:RELEASE.2018-10-18T00-28-58Z
MAINTAINER ome-devel@lists.openmicroscopy.org.uk

RUN wget https://dl.minio.io/client/mc/release/linux-amd64/mc -qO /usr/bin/mc && \
    chmod a+x /usr/bin/mc
ENV MINIO_ACCESS_KEY=accesskey
ENV MINIO_SECRET_KEY=secretkey

ADD initialise.sh /srv/
RUN /srv/initialise.sh

CMD ["server", "/srv"]
