# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added

### Changed

* Issue **#109** : Added packetSize="65536" property to AJP connector in server.xml template.

* Issue **#6** : Removed JODA Time library and replaced with Java 7 Time API.

* Issue **#8** : Fixed issue where `*.zip.bad` files were being picked up for proxy aggregation. Also it appears that old lock files would not have been cleared if a template pattern for file output was being used so this has also been altered.

* Issue **#9** : File permissions in distribution have now been changed to `0750` for directories and shell scripts and `0640` for all other files.

* Issue **#10** : Made changes to cope with Files.list() and Files.walk() returning streams that should be closed with 'try with resources' construct.

* Issue **#7** : The repository format can now be tailored to customise the directory structure.

## [initialOpenSourceRelease] - 2016-10-31
Intial open source release

[Unreleased]: https://github.com/gchq/stroom-proxy/compare/initialOpenSourceRelease...HEAD
