#!/bin/sh
set -eux

PORT=31836
PLATFORM=`uname | tr '[:upper:]' '[:lower:]'`

[ -f minio ] || \
    curl -sfSo minio "https://dl.minio.io/server/minio/release/$PLATFORM-amd64/minio"
[ -f mc ] || \
    curl -sfSo mc "https://dl.minio.io/client/mc/release/$PLATFORM-amd64/mc"
chmod +x minio mc
./minio version
./mc version

export MINIO_ACCESS_KEY=accesskey MINIO_SECRET_KEY=secretkey
./minio server --address localhost:$PORT . &
sleep 2;

./mc config host add ome-common-java-minio-test http://localhost:$PORT ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY}

set +x

for SUFFIX in public private; do
    ./mc ls ome-common-java-minio-test/bioformats.test.$SUFFIX || \
        ./mc mb ome-common-java-minio-test/bioformats.test.$SUFFIX
    ./mc ls ome-common-java-minio-test/bioformats.test.$SUFFIX/single-channel.ome.tiff || \
        curl -sfS https://downloads.openmicroscopy.org/images/OME-TIFF/2016-06/bioformats-artificial/single-channel.ome.tiff | \
            ./mc pipe ome-common-java-minio-test/bioformats.test.$SUFFIX/single-channel.ome.tiff
    # 2MB file for testing seek
    for n in `seq 65536`; do printf '.% 30d\n' $n; done | \
        ./mc pipe ome-common-java-minio-test/bioformats.test.$SUFFIX/2MBfile.txt
done

./mc policy public ome-common-java-minio-test/bioformats.test.public
