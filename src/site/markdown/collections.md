#Overview

Mellowtech Core Collections is a set of classes for disc based key-value maps. It supports both sorted and hashed maps.
The hash map implementation is still rough around the edges so the sorted map is preferred.

Most implementation was inspired by the book File Structures (Michael Folk, Bill Zoellick). This is an excellent
introduction to the underlying principles of modern databases. 

The Collections API comes in two flavors. Firstly you can directly use the underlying disc maps (BTree and ExtendibleHashTable)
and secondly you can use the *java.util.collections.Map* abstraction.

**All maps are unsynchronized**


##BTree

There are two main implementations of *org.mellowtech.core.collections.BTree*.

**BTreeImp (deprecated)** - implements a btree using two files - one to store the index and one to store the key/values. *BTreeImp* does not use any in-memory buffer and all write
operations are always directly written to file. This implementation is suitable when you
* don't have extreme requirements on speed
* need to persist the tree index on disc
* when you have limited memory available
* when your index can grow very big (which is typically not very likely)

**HybridTree** - The index of this tree is always kept in memory as a java.util.TreeMap. Every time the tree
is opened the index will be recrated from the tree's key/value file. This tree offers good performance and robustness.

The MemMappedBPTreeImp is suitable when you
* need a fast disc based tree
* are not memory constrained
* when you dont frequently open/close your tree

The simplest way of creating a BTree is to use *org.mellowtech.core.collections.BTreeBuilder* The BTreeBuilder creates
various HybridTrees.

```java
StringCodec strCodec = new StringCodec();
IntCodec intCodec = new IntCodec();
Path dir = Paths.get("/tmp");
BTreeBuilder builder = new BTreeBuilder();
BTree<String, Integer> db;
db = builder.memoryMappedValues(true).build(strCodec, intCodec, dir, "treemap");
    
BTree bt = new BTreeBuilder().valuesInMemory(true).build("someFileName",new CBString(), new CBString())
```

Would create a BTree that have its values memory mapped and all other options set to its default values

##ExtendibleHashTable

To be written

##Collections

The _BTree_ and _BMap_ APIs are primarily used as a basis for higher level constructs. In most situations
you as a developer should use the disc based collections that implement java.util.Map and java.util.NavigableMap.

These collections implement the full Java Collections Map APIs with additional methods for saving/opening your
disc based collection.


DiscMap comes with 2 concrete implementations (one for hashed maps and one for sorted maps). DiscMap extends the Map Api while SortedDiscMap extends the NavigableMap API. Views (submaps) works in the same
as you would expect.

```java
DiscMapBuilder builder = new DiscMapBuilder();
builder.blobValues(false);

SortedDiscMap <String, Integer> db = builder.blobValues(false).sorted(String.class, Integer.class, "/tmp/discbasedmap");
DiscMap <String, String> db1 = builder.blobValues(true).hashed(String.class, String.class, "tmp/hashbasedmap");

//or more generically (in which case you would have to cast to SortedDiscMap)
db = (SortedDiscMap<String, Integer>) builder.blobValues(false).build(String.class, Integer.class, "/tmp/discbasedmap", true);
db1 = builder.blobValues(true).build(String.class, String.class, "tmp/hashbasedmap", false);
  
```

If you prefer to work with the underlying structure directly you can do this as well using the tree and hash builders

```java
BTreeBuilder builder = new BTreeBuilder();
BTree<String, Integer> db;
Path dir = Paths.get("/tmp");
db = builder.memoryMappedValues(true).build(new StringCodec(), new IntCodec(), dir,"treemap");

EHTableBuilder ehbuilder = new EHTableBuilder();
BMap<String, String> db1;
db1 = ehbuilder.inMemory(true).blobValues(true).build(new StringCodec(), new StringCodec(),"/tmp/hashmap");
```

Would create the same underlying structure as the DiscMaps above

##Example
In this example we will build on the data we sorted in the [sorting](sorting.html) section. The idea is to create a
BTree that holds all unique words and the number of times it occurs. _Given large enough data it is a lot faster to
sort it first and then insert it than continuously update a counter value of a key in a tree_.

The first thing we need to implement is an iterator that emits String,Integer pairs based on a sorted input file.

```java
  
  static class WordCountIter implements Iterator<KeyValue<String,Integer>> {
    CodecInputStream<String> sis;
    KeyValue<String, Integer> next = null;
    String nextWord = null;
    String prev = null;

    public WordCountIter(CodecInputStream<String> sis) {
      this.sis = sis;
      next = new KeyValue<>();
      try {
        nextWord = sis.next();
        prev = nextWord;
      } catch (IOException e) {
        throw new Error("could not read");
      }
      getNext();
    }

    private void getNext() {
      if (nextWord == null) {
        next = null;
        return;
      }
      next = new KeyValue<>();
      int count = 1;
      next.setKey(nextWord);
      try {
        while (true) {
          nextWord = sis.next();
          if (nextWord == null || nextWord.compareTo(next.getKey()) != 0) {
            break;
          }
          count++;
          prev = nextWord;
        }
      } catch (IOException e) {
        throw new Error("could not read");
      }
      next.setValue(count);
    }

    public boolean hasNext() {
      return nextWord != null;
    }

    public KeyValue<String, Integer> next() {
      KeyValue<String, Integer> tmp = next;
      getNext();
      return tmp;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
```

The iterator takes as input a sorted file of serialized Strings and produce String,Integer pairs. Again, observe that the iterator
relies on a sorted input file.

Once we have our iterator in place it is simple enough to generate our tree

```java
StringCodec strCodec = new StringCodec();
IntCodec intCodec = new IntCodec();
BTreeBuilder builder = new BTreeBuilder();
BTree<String, Integer> tree;
Path dir = Paths.get("/tmp/btree");
tree = builder.build(strCodec, intCodec, dir, "english");
CodecInputStream<String> sis = new CodecInputStream<>(new FileInputStream("/tmp/english-sorted.bs"), strCodec);
WordCountIter iter = new WordCountIter(sis);
tree.createTree(iter);
tree.close();
```

To make sure that everything works as expected we could for instance iterate over the tree and print the results

```java
BTreeBuilder builder = new BTreeBuilder();
BTree <String, Integer> tree;
Path dir = Paths.get("/tmp/btree");
tree = builder.build(new StringCodec(), new IntCodec(), dir, "english");
Iterator <KeyValue<String, Integer>> iter = tree.iterator();
while(iter.hasNext()){
  KeyValue <String, Integer> kv = iter.next();
  System.out.println(kv.getKey()+": "+kv.getValue());
}
```








