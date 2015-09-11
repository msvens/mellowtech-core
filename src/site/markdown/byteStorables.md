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
own BStorable objects since the API comes with a lot of wrappers for common data types


##Using Built in BStorables

The library comes with a set of built in BStorable types to handle most
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

```java
  public static void serialize(){
    CBInt firstInt = new CBInt(1);
    ByteBuffer bb = firstInt.to();
    CBInt secondInt = firstInt.from(bb);
    System.out.println(firstInt.get()+" "+secondInt.get());
  }
```

In the second example (below) we are using the CBMixedList to store a list of primitive BStorables.

```java
 public static void list(){
    CBMixedList list = new CBMixedList();
    list.add(1);
    list.add("a string");
    list.add(new Long(100));
    list.add(true);

    ByteBuffer bb = list.to();
    list.clear();

    //don't create a new object
    bb.flip();
    list = list.from(bb);
    Integer first = (Integer) list.get(0);
    String second = (String) list.get(1);
    Long third = (Long) list.get(2);
    Boolean forth = (Boolean) list.get(3);
    System.out.println(first+" "+second+" "+third+" "+forth);
  }
```

It is easy to see how you can use BStorables as a way of doing deep copies

```java
ByteBuffer bb = BStorable.to();
bb.flip();
BStorable.fromBytes(bb)
```

This creates a true copy of your object. Since this is a common function it is also directly implemented in BStorable as

```java
BStorable copy = BStorable.deepCopy();
```


##Creating BStorables

In many situations using the in-built BStorables are enough. However, if
you need a more complex structure you will have to implement it. Again,
this is the difference from e.g. java.io.Serializable. It is a little
bit more work but it typically offers better performance. _BStorable_
contains a lot of methods to assist the developer. At a minimum you are
required to implement four methods and the empty constructor in your subclass.

###Subclassing BStorable

Lets implement a BStorable that contains an integer and a string value

```java
public class Container1 extends BStorable <Container1, Container1> {

  private CBInt f1 = new CBInt();
  private CBString f2 = new CBString();

  public Container1(){;}

  public Container1(Integer field1, String field2){
    f1.set(field1);
    f2.set(field2);
  }

  @Override
  public Container1 from(ByteBuffer bb) {
    Container1 toRet = doNew ? new Container1();
    CBUtil.getSize(bb, false);
    toRet.f1 = toRet.f1.from(bb); //no need to create a new object
    toRet.f2 ) toRet.f1.from(bb); //no need to create a new object
    return toRet;
  }

  @Override
  public void to(ByteBuffer bb) {
    CBUtil.putSize(f1.byteSize()+f2.byteSize(), bb, false);
    f1.toBytes(bb);
    f2.toBytes(bb);
  }

  @Override
  public int byteSize() {
    return CBUtil.byteSize(f1.byteSize()+f2.byteSize(), false);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CBUtil.peekSize(bb, false);
  }
}
```

Later you can simply use Container1 as any of the predefined _ByteStorables_

```java
  public static void testContainer1(){
    Container1 c1 = new Container1(10,"ten");
    Container1 c2 = c1.deepCopy();
    System.out.println("testContainer1: "+c2.f1+" "+c2.f2);
  }
```


A couple of important things to note when you create your own BStorables

1. 	You have to be able to determine the byte size within the first 4 bytes of
	the serialized object (that is why we include a size indicator

1.	When calculating the byteSize don't forget to include any bytes that
	a size indicator would occupy

1.	When reading the byteSize from a ByteBuffer your BStorable should
	not change the position in the ByteBuffer (that is why we used the utility
	function <CBUtil.peekSize>

1.	BStorables should implement the empty constructor

###Using CBAuto

As an alternative to the above pattern where you implment the to/from bytes yourself you can use a CBAuto or CBRecord
to simplify things. The first, CBAuto is the simplest but it will not allow you to separate the BStorable from the 
data you want to encapsulate. For both CBAuto and CBRecord the only caveat is that they will only support built in
types (string, int, double, etc.)

```java
public class Container2 extends CBAuto <Container2> {

  @BSField(2) private Integer f1;
  @BSField(1) private String f2;

  public Container2(){
    super();
  }

  public Container2(Integer field1, String field2){
    this();
    this.f1 = field1;
    this.f2 = field2;
  } 
}
```


###Using CBRecord and AutoRecord

So similar to CBAuto the CBRecord simplifies things for you and it has the added benefit of separating your model (AutoRecord)
from the BStorable implementation

```java
public class Container3 extends CBRecord <Container3.Record, Container3> {

  static class Record implements AutoRecord {
	  @BSField(2) public Integer f1;
	  @BSField(1) public String f2;
  }

  @Override
  protected Record newA() {
	return new Record();
  }

}
```

The idea with the CBRecord/AutoRecord pattern is to keep a clean separation of the BStorable
(CBRecord) and the object (AutoRecord) it wraps. In other words the actual data record is a regular
java object without any notion of BStorables. There are a couple of things to be aware of when using this pattern

* your wrapping CBRecord has to implement the newT function
* your wrapping CBRecord has to call the empty constructor of super - if it implements it's own constructors

##Comparing With BComparable

The original (and still) main purpose of the Mellowtech Core library
was to offer functionality to sort and store objects on disc. In order
to do this we have to be able to compare objects. In many situations
the following would be sufficient

```java
new CBString("string").get().equals("string")
```

That is, you do your comparison on an object level. However, if you many
million objects that have been serialized this scheme might impact
performance quite a bit if you constantly have to create objects when
doing comparisons.

_BComparable_ allows do do object comparison on a byte level. All in-built
BStorables (except from CBMixedList) are also BComparables.

```java
public static void compareStrings(){
  ByteBuffer str1 = new CBString("a string").to();
	ByteBuffer str2 = new CBString("a string").to();
	System.out.println(new CBString().byteCompare(0, str1, 0, str2));
}
```

In the above example we compare two string on a byte level that are stored in 2 different ByteBuffers.

A slightly more involved use case of how to use the BComparable pattern would be two compare
objects that are stored in the same ByteBuffer. An example of this could look something like this

```java
public static void compareInSameBuffer(){
  CBString str1 = new CBString("a string 1");
  CBString str2 = new CBString("a string");
  ByteBuffer bb = ByteBuffer.wrap(new byte[str1.byteSize()+str2.byteSize()]);
  str1.to(bb);
  str2.to(bb);
  System.out.println(new CBString().byteCompare(0, str1.byteSize(), bb));
}
```

