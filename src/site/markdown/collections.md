#Overview

Mellowtech Core Collections is a set of classes for disc based key-value maps. It supports both sorted and hashed maps.
The hash map implementation is still rough around the edges so the sorted map is preferred.

Most implementation was inspired by the book File Structures (Michael Folk, Bill Zoellick). This is an excellent
introduction to the underlying principles of modern databases. 

The Collections API comes in two flavors. Firstly you can directly use the underlying disc maps (BTree and ExtendibleHashTable)
and secondly you can use the *java.util.collections.Map* abstraction.

**All maps are unsynchronized**


##BTree

There are two main implementations of *org.mellowtech.core.collections.tree.BTree*.

**BTreeImp** - implements a btree using two files - one to store the index and one to store the key/values. *BTreeImp* does not use any in-memory buffer and all write
operations are always directly written to file. This implementation is suitable when you
* don't have extreme requirements on speed
* when you have limited memory available
* when your index can grow very big (which is typically not very likely)

**MemMappedBPTreeImp** - implements a btree using one backing file (org.mellowtech.core.io.SplitRecordFile). The index will
always be kept in a memory mapped buffer. Based on configuration the Key/Value part of the BTree can also be memory mapped.
The MemMappedBPTreeImp is suitable when you
* know that the index will not grow beyond a certain point
* when you need a fast btree

The simplest way of creating a BTree is to use *org.mellowtech.core.collections.tree.BTreeBuilder*

```
BTree bt = new BTreeBuilder().valuesInMemory(true).build("someFileName",new CBString(), new CBString())
```

Would create a BTree that have its values memory mapped and all other options set to its default values

##ExtendibleHashTable

To be written

##Collections

If you prefer to work with normal objects (i.e. not ByteStorables) the *org.mellowtech.core.collections.DiscMap* API is for you.
It extends *java.util.Map* and adds a couple of methods for closing/saving a map to disc as well as 2 entry iterators.

DiscMap comes with 2 concrete implementations (DiscBasedMap and DiscBasedHashMap) that use BTree and ExtendibleHashTable
respectively. DiscMaps use Mappings to convert objects to and from ByteStorables so when you use the DiscMap API you need to
make sure that there is a corresponding Mapping for it. mellowtech.core comes with a default set of Mappings that can be
extended with user defined mappings

```
BCMapping m = MappingFactory.createMapping(String.class);
DiscMap map = new DiscBasedMap(m,m,"someFileName",new BTreeBuilder().valuesInMemory(true));
map.put("firstKey", "firstValue");
```

Would create the same underlying structure as the previous example but expose it is as an ordinary map

##Example
In this example we will build on the data we sorted in the [sorting](sorting.html) section. The idea is to create a
BTree that holds all unique words and the number of times the occur. Given large enough data it is a lot faster to
sort it first and then insert it than continuously update a counter value of a key in a tree.

The first thing we need to implement is an iterator that emits CBString,CBInt pairs based on a sorted input file.

```
  static class WordIter implements Iterator<KeyValue<CBString, CBInt>> {

    StorableInputStream<CBString> sis;
    KeyValue<CBString, CBInt> next = null;
    CBString nextWord = null;
    CBString prev = null;

    public WordIter(StorableInputStream<CBString> sis) {
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
      next.setValue(new CBInt(count));
    }

    public boolean hasNext() {
      return nextWord != null;
    }

    public KeyValue<CBString, CBInt> next() {
      KeyValue<CBString, CBInt> tmp = next;
      getNext();
      return tmp;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
```
The iterator takes as input a sorted file of CBStrings and produce CBString,CBInt pairs. Again, observe that the iterator
relies on a sorted input file.

Once we have our iterator in place it is simple enough to generate our tree

```
BTreeBuilder builder = new BTreeBuilder();
BTree <CBString, CBInt> tree = builder.indexInMemory(true).build(new CBString(), new CBInt(), "/some/path/to/tree");
StorableInputStream <CBString> sis = new StorableInputStream <>(new FileInputStream("/tmp/english-sorted.bs"), new CBString());
WordIter iter = new WordIter(sis);
tree.createIndex(iter);
tree.close();
```

To make sure that everything works as expected we could for instance iterate over the tree and print the results

```
BTreeBuilder builder = new BTreeBuilder();
BTree <CBString, CBInt> tree = builder.indexInMemory(true).build(new CBString(), new CBInt(), "/some/path/to/tree");
Iterator <KeyValue<CBString,CBInt>> iter = tree.iterator();
while(iter.hasNext()){
  KeyValue <CBString, CBInt> kv = iter.next();
  System.out.println(kv.getKey()+": "+kv.getValue());
}
```








