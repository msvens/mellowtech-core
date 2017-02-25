# Mellowtech Core #

[![Build Status](https://travis-ci.org/msvens/mellowtech-core.svg?branch=master)](https://travis-ci.org/msvens/mellowtech-core)
[![Maven Central](https://img.shields.io/maven-central/v/org.mellowtech/core.svg)](https://maven-badges.herokuapp.com/maven-central/org.mellowtech/core)


##Overview

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

* [Codecs](src/site/markdown/codecs.md)
* [IO](src/site/markdown/IO.md)
* [Sorting](src/site/markdown/sorting.md)
* [Collections](src/site/markdown/collections.md)

##Releases

###3.0.7
* Added [HybridTree](https://github.com/msvens/mellowtech-core/blob/master/src/main/java/org/mellowtech/core/collections/impl/HybridTree.java). This tree
keeps the index totally in memory using a java treemap that is rebuilt every time the Tree is opened. The
hybridtree offers very good performance and is also robust because there is no complicated logic to ensure
integrity of data blocks and index
* Added [MultiBlockFile](https://github.com/msvens/mellowtech-core/blob/master/src/main/java/org/mellowtech/core/io/impl/MultiBlockFile.java). This
blockfile splits the records into even sized multiple files. It does not have an upper bound of blocks and
does not require a bitmap for used/deleted blocks. Each block instead has a magic marker in the first four
bytes indicating if the block has been deleted. This marker will likely increase in number of bytes to decrease
the likelyhood of actual data being read as the deleted marker
* Fixed Bug that broke the BTreeMap iterators when specifing an exclusive from key.

###3.0.3 - Hardening
* Added extensive unit testing for key classes. Around 1000 tests were added
* Added proper support of java Map and NavigableMap Apis. Now Map views are proper views of the underlying disc map
* Continued deprecation of old and unused classes
* Refactored collections package with a more clean separation of Api and implementation

###3.0.2 - To Maven Official
* Initial release on official Maven repository
* Major update to ByteStorable API
* First release to require java 8
