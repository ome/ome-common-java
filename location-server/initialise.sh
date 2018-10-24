#!/bin/sh
set -eux

minio server /srv &
sleep 2;

mc config host add minio http://localhost:9000 ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY}

for SUFFIX in public private; do
  mc mb minio/bioformats.test.$SUFFIX
  wget http://downloads.openmicroscopy.org/images/OME-TIFF/2016-06/bioformats-artificial/single-channel.ome.tiff -O - | mc pipe minio/bioformats.test.$SUFFIX/single-channel.ome.tiff
done

mc policy public minio/bioformats.test.public
