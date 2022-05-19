6.0.8 (2022-05-19)
------------------

-  Bump logback dependencies to `ch.qos.logback:logback-core:1.2.9` and `ch.qos.logback:logback-classic:1.2.9` ([#61](https://github.com/ome/ome-common-java/pull/61))
-  Add JDK 17 to the testing matrix ( [#60](https://github.com/ome/ome-common-java/pull/60))

6.0.7 (2021-06-18)
------------------

-  Bump guava dependency to `com.google.guava:guava:29.0-jre` ([#55](https://github.com/ome/ome-common-java/pull/55))
-  Bump logback dependencies to `ch.qos.logback:logback-core:1.2.0` and `ch.qos.logback:logback-classic:1.2.0` ([#58](https://github.com/ome/ome-common-java/pull/58))
-  Switch from Travis CI to GitHub actions ( [#53](https://github.com/ome/ome-common-java/pull/53))

6.0.5 (2020-07-24)
------------------

-  Bump kryo dependency to `com.esotericsoftware:kryo:4.0.2` ([#50](https://github.com/ome/ome-common-java/pull/50))
-  Fix minio version command in `start-location.sh` ([#51](https://github.com/ome/ome-common-java/pull/51), [#52](https://github.com/ome/ome-common-java/pull/52))

6.0.4 (2019-10-22)
------------------

-  DateTools: clarify expected units for timestamp passed to `convertDate` ([#45](https://github.com/ome/ome-common-java/pull/45))
-  DateTools: attempt to parse invalid dates with Locale.US ([#48](https://github.com/ome/ome-common-java/pull/48))

6.0.3 (2019-05-16)
------------------

-  ServiceFactory: set `services` field to transient ([#44](https://github.com/ome/ome-common-java/pull/44))

6.0.2 (2019-05-08)
------------------

-  Also exclude jsr305 dependency from minio ([#43](https://github.com/ome/ome-common-java/pull/43))

6.0.1 (2019-04-19)
------------------

- Exclude `io.minio:minio` transitive dependencies ([#40](https://github.com/ome/ome-common-java/pull/40))
- Ignore Eclipse metadata files ([#41](https://github.com/ome/ome-common-java/pull/41))
- Bump `com.google.guava:guava` to version 27.1 ([#42](https://github.com/ome/ome-common-java/pull/42))

6.0.0 (2019-02-06)
------------------

- Initial support for handling S3 locations ([#33](https://github.com/ome/ome-common-java/pull/33)[#39](https://github.com/ome/ome-common-java/pull/39))
  * Add `S3Handle` class providing random access to S3 buckets
  * Add `S3ClientService` interface along with its implementation using `minio` as a dependency
  * Add `exists()` method to `IRandomAccess` and all implementations
  * Register S3Client service interface and implementation
  * Add unit tests and update Travis to start a S3 server and run all tests
- Update `Location.getParentFile()` contract to match `File.getParentFile` ([#38](https://github.com/ome/ome-common-java/pull/38))
- NIOFileHandle: don't read from the file to fill a trash buffer ([#34](https://github.com/ome/ome-common-java/pull/34))
- Add initial API for downsampling images ([#35](https://github.com/ome/ome-common-java/pull/35))
  * add `IImageScaler` interface
  * add `SimpleImageScaler` implementation
- Attempt to check for incorrect length after RandomAccessFile.setLength ([#29](https://github.com/ome/ome-common-java/pull/29))
- StreamHandle: fix subtle bug when seeking more than 1 MB backward ([#22](https://github.com/ome/ome-common-java/pull/22))
- Add `skipBytes(long)` method to `IRandomAccess` and all implementations ([#27](https://github.com/ome/ome-common-java/pull/27))


5.3.7 (2018-10-12)
------------------

- Reduce memory usage when reading large text files ([#24](https://github.com/ome/ome-common-java/pull/24))
- `DataTools`: fix bug in scientific notation parsing ([#30](https://github.com/ome/ome-common-java/pull/30))
- Deprecate `CaseInsensitiveLocation`([#31](https://github.com/ome/ome-common-java/pull/31))


5.3.6 (2018-08-02)
------------------

- Fix Javadoc warnings ([#26](https://github.com/ome/ome-common-java/pull/26))
- Build and Maven updates ([#25](https://github.com/ome/ome-common-java/pull/25), [#28](https://github.com/ome/ome-common-java/pull/28))


5.3.5 (2018-03-05)
------------------

- Update Maven plugin versions ([#16](https://github.com/ome/ome-common-java/pull/16))


5.3.4 (2018-01-04)
------------------

- Revert 5.3.3 changes to RandomAccessFile.setLength in NIOFileHandle ([#21](https://github.com/ome/ome-common-java/pull/21))

5.3.3 (2017-10-20)
------------------

- Reduce calls to RandomAccessFile.setLength in NIOFileHandle ([#15](https://github.com/ome/ome-common-java/pull/15))
- Fix LocationTest unit tests ([#13](https://github.com/ome/ome-common-java/pull/13))

5.3.2 (2017-06-29)
------------------

- Register OME codecs service ([#11](https://github.com/ome/ome-common-java/pull/11))
- Clean up POM ([#9](https://github.com/ome/ome-common-java/pull/9))

5.3.1 (2017-11-06)
------------------

- Register JPEG-XR service ([#8](https://github.com/ome/ome-common-java/pull/8))
- Cleanup references to the OME Artifactory ([#7](https://github.com/ome/ome-common-java/pull/7))

5.3.0 (2016-10-13)
------------------

- Add Maven and Travis infrastructure ([#1](https://github.com/ome/ome-common-java/pull/1), [#2](https://github.com/ome/ome-common-java/pull/2), [#3](https://github.com/ome/ome-common-java/pull/1))
- Initial decoupling ome-common components from Bio-Formats
