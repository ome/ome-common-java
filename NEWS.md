6.0.0-m2 (2018-11-30)
---------------------

- Add  `S3Handle` ([#33](https://github.com/ome/ome-common-java/pull/33))
  * Add `minio` as a dependency
  * Add `exists()` method to `IRandomAccess` and all implementations
  * Add unit tests and update Travis to start a S3 server and run all tests
- NIOFileHandle: don't read from the file to fill a trash buffer ([#34](https://github.com/ome/ome-common-java/pull/34))
- Add initial API for downsampling images ([#29](https://github.com/ome/ome-common-java/pull/29))
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
