## Nginous application server
The Nginious Application Server provides a platform that focuses on making
development of web applications easy. At the heart of Nginious is a flexible
reloading system where source code and resource changes are update immediately.
An Ecplise plugin is also provided which integrates Nginious into the Eclipse
development environment.

## Documentation
See the current [Javadoc][] and [reference docs][].

## Building from source
The Nginious Application Server uses [Gradle][] for building. In the instructions
below, [`./gradlew`][] is invoked from the root of the source tree and serves as
a cross-platform, self-contained bootstrap mechanism for the build. The only
prerequisites are [Git][] and JDK 1.6+.

### check out sources
`git clone git://github.com/bojanp/Nginious.git`

### compile, test and build
`./gradlew build`

## License
The Nginious Application Server is released under version 2.0 of the [Apache License][].

[Javadoc]: http://www.nignious.com/docs/api
[reference docs]: http://www.nginious.com/doc/reference
[Gradle]: http://gradle.org
[`./gradlew`]: http://vimeo.com/34436402
[Git]: http://help.github.com/set-up-git-redirect
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
