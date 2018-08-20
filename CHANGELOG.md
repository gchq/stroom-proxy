# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added

### Changed

## [v5.1.1] - 2018-08-20

* Issue **#26** : Fix for PathCreator replace method.

## [v5.1.0] - 2018-01-19

* Deleted transient file stroom-proxy-util/src/main/resources/META-INF/stroom-util-build.properties

* Change docker image to use openjdk:8u151-jre-alpine as a base

## [v5.1-beta.11] - 2018-01-16

* Issue **#24** : Replaced instances of `Files.walk()` with `Files.walkFileTree()`. `Files.walk()` throws errors if any files are deleted or are not accessible during the walk operation. This is a major issue with the Java design for walking files using Java 8 streams. To avoid this issue `Files.walkFileTree()` has now been used in place of `Files.walk()`.

* Issue **#2** : Fixed status JSP page.

## [v5.1-beta.10] - 2017-07-25

* Issue **#22** : Moved into correct JAR.

## [v5.1-beta.9] - 2017-07-25

* Issue **#22** : Added ImportKey class back into proxy.

## [v5.1-beta.8] - 2017-07-07

* Issue **#21** : Added in runtime dependency on AspectJ.

## [v5.1-beta.7] - 2017-05-23

* Issue **#19** : Fixed error thrown when attempting to remove transient lock files.

## [v5.1-beta.6] - 2017-05-23

* Issue **#17** : Fixed old repository scanning bug on restart.

## [v5.1-beta.5] - 2017-05-22

* Issue **#15** : Removed default memory options from env.sh.

* Issue **#16** : Proxy now prompts for the repository format during setup.

## [v5.1-beta.4] - 2017-05-22

* Issue **#109** : Added packetSize="65536" property to AJP connector in server.xml template.

* Issue **#6** : Removed JODA Time library and replaced with Java 7 Time API.

* Issue **#8** : Fixed issue where `*.zip.bad` files were being picked up for proxy aggregation. Also it appears that old lock files would not have been cleared if a template pattern for file output was being used so this has also been altered.

* Issue **#9** : File permissions in distribution have now been changed to `0750` for directories and shell scripts and `0640` for all other files.

* Issue **#10** : Made changes to cope with Files.list() and Files.walk() returning streams that should be closed with 'try with resources' construct.

* Issue **#7** : The repository format can now be tailored to customise the directory structure.

## [initialOpenSourceRelease] - 2016-10-31
Intial open source release

[Unreleased]: https://github.com/gchq/stroom/compare/v5.1.1...HEAD
[v5.1.1]: https://github.com/gchq/stroom/compare/v5.1.0...v5.1.1
[v5.1.0]: https://github.com/gchq/stroom/compare/v5.1-beta.11...v5.1.0
[v5.1-beta.11]: https://github.com/gchq/stroom/compare/v5.1-beta.10...v5.1-beta.11
[v5.1-beta.10]: https://github.com/gchq/stroom/compare/v5.1-beta.9...v5.1-beta.10
[v5.1-beta.9]: https://github.com/gchq/stroom/compare/v5.1-beta.8...v5.1-beta.9
[v5.1-beta.8]: https://github.com/gchq/stroom/compare/v5.1-beta.7...v5.1-beta.8
[v5.1-beta.7]: https://github.com/gchq/stroom/compare/v5.1-beta.6...v5.1-beta.7
[v5.1-beta.6]: https://github.com/gchq/stroom/compare/v5.1-beta.5...v5.1-beta.6
[v5.1-beta.5]: https://github.com/gchq/stroom/compare/v5.1-beta.4...v5.1-beta.5
[v5.1-beta.4]: https://github.com/gchq/stroom/releases/tag/v5.1-beta.4
