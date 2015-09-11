#Overview

Mellowtech Core IO is an IO abstraction layer for working with files that contains data organized as records. The API
is defined in RecordFile and SplitRecordFile respectively. A SplitRecordFile is a RecordFile with some added functionality.

You can create a record file with any of the following properties

* **DISC** - This type of record file is backed up by a normal file and all read and writes are directly reflected in the file
* **MEM** - This type of record file uses NIO memory maps for reading and writing records
* **SPLIT** - This type of record file splits the file into two distinct regions of records. All read/writes are directly reflected in the file
* **MEM_SPLIT** - Same as split files but uses NIO memory maps for reading and writing records

Common to all record files is that all have a *reseve* space in the beginning that can be used for storing additional non-record data.
All record files are also backed by a single file and uses a memory mapped bit buffer for keeping track of free/full records. This
means that a record file will always have a size overhead close to (num records / 8) bytes. So if you create a RecordFile with a
capacity of 1M records it will have a size overhead of roughly 125K bytes. For MEM files this is slightly different since it sets the file size
to the largest mapped region. So always expect file sizes to be larger than the actual data you store.

**Never assume that the various record files are binary compatible**

##Creating and Using RecordFiles

The preferred way of creating files is to use the RecordFileBuilder, then you don't need to worry about which underlying
implementation to use. Lets say you want to instantiate a memory mapped RecordFile.

```java
RecordFileBuilder builder = new RecordFileBuilder();
builder.blockSize(2048).mem().reserve(0).maxBlocks(1024*1024);
RecordFile rf = builder.build("/tmp/myfile.rf");
System.out.println("free blocks: "+rf.getFreeBlocks());
```

This would create a memory mapped record file that can store 1M of records of size 2048K. To add records you use the insert method

```java
byte[] b = new CBString("first string").to().array();
byte[] b1 = new CBString("second string").to().array();
rf.insert(100000, b);
rf.insert(512, b1);
```

To retrieve your records you use the get method

```java
b = rf.get(100000);
CBString str = new CBString().from(b, 0);
System.out.println(str.get());
```

You can also iterate over your record file. This will iterate over all set records

```
CBString tmp = new CBString();
for(Iterator<Record>iter = rf.iterator(); iter.hasNext();){
  Record r = iter.next();
  System.out.println("record: "+r.record+" "+tmp.from(r.data,0));
}
```

###Dynamically Sized Records

In some circumstances you will need a way of allocating records that can grow and shrink in size. That is, 
you might need one record that is 100K in size and another that is only 1K in size. Creating a RecordFile with 
record sizes to handle your largest record might result in a lot of wasted space (and you might not even know 
the maximum size to start with). For this you can use the SpannedBlockFile and IteratingSpannedBlockFile.

The easiest way to create a SpannedBlockFile is to use the RecordFileBuilder

```java
builder.span(true).iterate(true)
```

This would create a spanning block file that you can iterate over. You can turn any of the underlying block files 
(mem, split, disc, memsplit) to spanned one.

**Observe** that a spanned block file that you can iterate over will use the reserve space 
to store a bitset so you will not be able to use the reserve space for other purposes



