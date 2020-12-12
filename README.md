# OME Common Java

[![Actions Status](https://github.com/ome/ome-common-java/workflows/Maven/badge.svg)](https://github.com/ome/ome-common-java/actions)
[![Maven Central](https://img.shields.io/maven-central/v/org.openmicroscopy/ome-common.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.openmicroscopy%22%20AND%20a%3A%22ome-common%22)
[![Javadocs](http://javadoc.io/badge/org.openmicroscopy/ome-common.svg)](http://javadoc.io/doc/org.openmicroscopy/ome-common)

Common I/O, date parsing, and XML processing classes for OME Java components.


More information
----------------

For more information, see the [Bio-Formats web
site](https://www.openmicroscopy.org/bio-formats/).


Pull request testing
--------------------

We welcome pull requests from anyone, but ask that you please verify the
following before submitting a pull request:

 * verify that the branch merges cleanly into ```master```
 * verify that the branch builds using Maven
 * verify that the branch does not use syntax or API specific to Java 1.9+
 * run the unit tests (```mvn test```) and correct any failures
 * make sure that your commits contain the correct authorship information
 * make sure that the commit messages or pull request comment contains
   sufficient information for the reviewer(s) to understand what problem was
   fixed and how to test it
