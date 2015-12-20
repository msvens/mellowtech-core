#Overview

Mellowtech Core is a set of components that we use for working with disc based
and byte based manipulation of Objects. It is typically useful for any scenario
that involves

* a need for storing your collections on disc

* sorting large collections of objects that will not fit in memory

* consistent access to files that contains objects

* fast transformation of objects from and to byte representations (instead of using normal Java Serialization)

In our own work we have used this library mainly for developing search engines.

The library itself was initially created by Martin Svensson and Rickard Cöster while
working at the Swedish Institue of Computer Science in 2002. Since then it has undergone
a lot of changes and enhancements and is now maintained and developed by mellowtech.org.

Go to the detailed descriptions for usage instructions

* [ByteStorables](src/site/markdown/byteStorables.md)
* [IO](src/site/markdown/IO.md)
* [Sorting](src/site/markdown/sorting.md)
* [Collections](src/site/markdown/collections.md)

##Releases

###3.0.3 - Hardening
* Added extensive unit testing for key classes. Around 1000 tests were added
* Added proper support of java Map and NavigableMap Apis. Now Map views are proper views of the underlying disc map
* Continued deprecation of old and unused classes
* Refactored collections package with a more clean separation of Api and implementation

###3.0.2 - To Maven Official
* Initial release on official Maven repository
* Major update to ByteStorable API
* First release to require java 8

##Download

The core mellowtech.org API can be downloaded from the central maven repository


In your <pom.xml> include the following dependency


```
<dependency>
  <groupId>org.mellowtech</groupId>
  <artifactId>core</artifactId>
  <version>3.0.3</version>
</dependency>
```
