#Overview
As was explained in the introduction Mellowtech Core is basically an API
for supporting the developer in situations when in memory is not enough,
e.g. objects needs to have a byte based representation.

In Java this is typically achieved with the Serialization API so why did we
create an alternative? First and foremost, it was because of speed and to
force a programmer to implement the necessary methods. Second, to a large
extend Mellowtech Core works directly on ByteBuffers and the normal
Serialization API does not have in-built support for that. Finally, we also
wanted a way of comparing objects on a byte level, again something that the
Serialization API does not support.

Today there are very good alternatives to the in-built Serialization API, such as,
Google's Protobuf protocol. We are looking into how Mellowtech Core can support
that.

Before we go on you should note that you would typically not have to create your
own ByteStorable objects since the API comes with a lot of wrappers for common data types


##Using Built in ByteStorables

The library comes with a set of built in ByteStorable types to handle most
situations that you would need when serializing objects. The primitive types are:

* CBBoolean - stores a boolean
* CBByte - stores a Byte
* CBChar - stores a Character
* CBShort - stores a short
* CBInt - stores an int
* CBLong - stores a long
* CBFloat - stores a float
* CBDouble - stores a double
* CBString - stores a String
* CBByteArray - stores a byte[]

In addition to the primitive types you also have the following built ins

* CBList - implements the List Interface
* CBMap - implements the map Interface
* CBSortedMap - implements the map Interface
* CBPrimitiveObject - can hold any of the above
* CBRecord - base class for building complex types
* AutoRecord - interface for building complex types

In the first example we simply use the API to serialize an integer and read it back again.

```
  public static void serialize(){
    CBInt firstInt = new CBInt(1);
    ByteBuffer bb = firstInt.toBytes();
    CBInt secondInt = new CBInt();
    secondInt.fromBytes(bb, false);
    System.out.println(firstInt.get()+" "+secondInt.get());
  }
```

In the second example (below) we are using the CBMixedList to store a list of primitive ByteStorables.

```
  public static void list(){
    CBMixedList list = new CBMixedList();
    list.add(1);
    list.add("a string");
    list.add(new Long(100));
    list.add(true);

    ByteBuffer bb = list.toBytes();
    list.clear();

    //don't create a new object
    list.fromBytes(bb, false);
    Integer first = (Integer) list.get(0);
    String second = (String) list.get(1);
    Long third = (Long) list.get(2);
    Boolean b = (Boolean) list.get(3);

  }
```

It is easy to see how you can use ByteStorables as a way of doing deep copies

```
ByteBuffer bb = byteStorable.toBytes();
bb.flip();
byteStorable.fromBytes(bb, true)
```

This creates a true copy of your object. Since this is a common function it is also directly implemented in ByteStorable as

```
ByteStorable copy = byteStorable.deepCopy();
```


##Creating ByteStorables

In many situations using the in-built ByteStorables are enough. However, if
you need a more complex structure you will have to implement it. Again,
this is the difference from e.g. java.io.Serializable. It is a little
bit more work but it typically offers better performance. _ByteStorable_
contains a lot of methods to assist the developer. At a minimum you are
required to implement four methods and the empty constructor in your subclass.

###Subclassing ByteStorable

Lets implement a ByteStorable that contains an integer and a string value

```
public class Container1 extends ByteStorable <Container1> {

  private CBInt f1 = new CBInt();
  private CBString f2 = new CBString();

  public Container1(){;}

  public Container1(Integer field1, String field2){
    f1.set(field1);
    f2.set(field2);
  }

  @Override
  public ByteStorable <Container1> fromBytes(ByteBuffer bb, boolean doNew) {
    Container1 toRet = doNew ? new Container1() : this;
    bb.getInt(); //read past size indicator
    toRet.f1.fromBytes(bb, false); //no need to create a new object
    toRet.f2.fromBytes(bb, false); //no need to create a new object
    return toRet;
  }

  @Override
  public void toBytes(ByteBuffer bb) {
    bb.putInt(byteSize()); //write size
    f1.toBytes(bb);
    f2.toBytes(bb);
  }

  @Override
  public int byteSize() {
    int size = 4; //size indicator
    size += f1.byteSize();
    size += f2.byteSize();
    return size;
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return getSizeFour(bb); //read size indicator without moving pos in bb
  }
}
```

A couple of important things to note when you create your own ByteStorables

1. 	You have to be able to determine the byte size within the first 4 bytes of
	the serialized object (that is why we include a size indicator

1.	When calculating the byteSize don't forget to include any bytes that
	a size indicator would occupy (that is why we added 4 in the <byteSize()> method

1.	When reading the byteSize from a ByteBuffer your ByteStorable should
	not change the position in the ByteBuffer (that why we used the utility
	function <getSizeFour()>

1.	ByteStorables should implement the empty constructor

1.	The get/set methods are overwritten when your ByteStorable acts as a
	wrappper for another object (e.g. new CBInt(1).get() returns an Integer)


In most situations implementing the above 4 methods are fine. However,
if you need to fine tune the performance it might make sense to override
the other from/to byte methods

###Using CBRecord and AutoRecord

As an alternative to the above pattern where you implement the to/from bytes yourself
you can use the CBRecord/AutoRecord pattern if you need to a complex object. The only thing to watch
out for is that it only supports the built-in ByteStorables. The previous ByteStorable would
be implemented using this pattern in the following way

```
public class Container3 extends CBRecord <Container3.Record> {

  class Record implements AutoRecord {
	  @BSField(2) private Integer f1;
	  @BSField(1) private String f2;
  }

  @Override
  protected Record newT() {
	return new Record();
  }
}
```

The idea with the CBRecord/AutoRecord pattern is to keep a clean separation of the wrapping ByteStorable
(CBRecord) and the object (AutoRecord) it wraps. In other words the actual data record is a regular
java object without any notion of ByteStorables. There are a couple of things to be aware of when using this pattern

* your wrapping CBRecord has to implement the newT function
* your wrapping CBRecord has to call the empty constructor of super - if it implements it's own constructors

##Comparing With ByteComparable

The original (and still) main purpose of the Mellowtech Core library
was to offer functionality to sort and store objects on disc. In order
to do this we have to be able to compare objects. In many situations
the following would be sufficient

```
new CBString("string").get().equals("string")
```

That is, you do your comparison on an object level. However, if you many
million objects that have been serialized this scheme might impact
performance quite a bit if you constantly have to create objects when
doing comparisons.

_ByteComparable_ allows do do object comparison on a byte level. All in-built
ByteStorables (apart from CBMixedList) are also ByteComparables.

```
public static void compareStrings(){
  ByteBuffer str1 = new CBString("a string").toBytes();
	ByteBuffer str2 = new CBString("a string").toBytes();
	System.out.println(new CBString().byteCompare(0, str1, 0, str2));
}
```

In the above example we compare two string on a byte level that are stored in 2 different ByteBuffers.

A slightly more involved use case of how to use the ByteComparable pattern would be two compare
objects that are stored in the same ByteBuffer. An example of this could look something like this

```
public static void compareInSameBuffer(){
  CBString str1 = new CBString("a string 1");
  CBString str2 = new CBString("a string");
  ByteBuffer bb = ByteBuffer.wrap(new byte[str1.byteSize()+str2.byteSize()]);
  str1.toBytes(bb);
  str2.toBytes(bb);
  System.out.println(new CBString().byteCompare(0, str1.byteSize(), bb));
}
```

