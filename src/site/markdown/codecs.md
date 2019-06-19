# Overview
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
own Codecs since the API has built in support for the most common types


## Using built in codecs

The primitive types in Mellowtech core are

* boolean
* byte
* character
* short
* int
* long
* float
* double
* String
* byte[]
* UUID

The library comes with built in codecs for these java types and the library can do automaticaly handles
instances of these types without the need to provide any additional codes, for instance in the disc
based collections library


In addition to the fundamental codecs the library comes with the following codecs

* ObjectCodec for serializing any of the primitive types
* ListCodec for Lists
* SetCodec for Sets
* MapCodec for Maps
* MixedListCodec for Lists with objects that can be serialized using the ObjectCodec
* CBPrimitiveObject - can hold any of the above
* CBRecord - base class for building complex types
* AutoRecord - interface for building complex types

In the first example we simply use the API to serialize an integer and read it back again.

```java
  public static void serialize(){
    IntCodec codec = new IntCodec();
    int first = 1;
    ByteBuffer bb = codec.to(1);
    bb.flip();
    Integer second = codec.from(bb);
    System.out.println(first+" "+second);
  }
```

In the second example (below) we are using the MixedListCodec to store a list of primitive BStorables.

```java
 public static void list(){
    MixedListCodec codec = new MixedListCodec();
    List<Object> list = new ArrayList<>();
    list.add(1);
    list.add("a string");
    list.add(new Long(100));
    list.add(true);

    ByteBuffer bb = codec.to(list);
    list.clear();

    bb.flip();
    list = codec.from(bb);
    Integer first = (Integer) list.get(0);
    String second = (String) list.get(1);
    Long third = (Long) list.get(2);
    Boolean forth = (Boolean) list.get(3);
    System.out.println(first+" "+second+" "+third+" "+forth);
  }
```

It is easy to see how you can use Codecs as a way of doing deep copies

```java
ByteBuffer bb = codec.to(someValue);
bb.flip();
codec.from(bb)
```

This creates a true copy of your object. Since this is a common function it is also directly implemented in BCodec

```java
Object copy = codec.deepCopy(someObject);
```


## Serializing complex types

For most situations the built-in codecs are enough. However, if
you need a more complex structure you will have to implement your own codec. Again,
this is the difference from e.g. java.io.Serializable. It is more work but it 
typically offers better performance. _BCodec_ and _CodecUtil_
contains methods to assist the developer. At a minimum you are
required to implement four methods in your custom codec.

### Subclassing BCodec

Lets implement a Codec that serializes a class that contains an integer and string. We start with
implementing the container

```java
public class Container1 {

  public int f1;
  public String f2;

  public Container1(int f1, String f2){
    this.f1 = f1;
    this.f2 = f2;
  }
}
```
Next we implement the actual codec. Note that we are using _CodecUtil_ to handle byte size reading and writing

```java
public class Container1Codec implements BCodec<Container1> {

  //We are using the StringCodec for serialization of the string field
  BCodec<String> f2Codec = new StringCodec();

  @Override
  public int byteSize(Container1 c) {
    //The integer is of size 4
    return CodecUtil.byteSize(4 + f2Codec.byteSize(c.f2), true);
  }

  @Override
  public int byteSize(ByteBuffer bb) {
    return CodecUtil.peekSize(bb, true);
  }

  @Override
  public Container1 from(ByteBuffer bb) {
    CodecUtil.getSize(bb, true);
    int f1 = bb.getInt();
    String f2 = f2Codec.from(bb);
    return new Container1(f1,f2);
  }

  @Override
  public void to(Container1 c, ByteBuffer bb) {
    CodecUtil.putSize(4 + f2Codec.byteSize(c.f2), bb, true);
    bb.putInt(c.f1);
    f2Codec.to(c.f2, bb);
  }
}

```

Later you can serialize your container using your codec like this

```java
  public static void testContainer1(){
      Container1 c1 = new Container1(10,"ten");
      Container1Codec codec = new Container1Codec();
      Container1 c2 = codec.deepCopy(c1);
      System.out.println("testContainer1: "+c2.f1+" "+c2.f2);
    }
```


A couple of important things to note when you define your own codecs

1. 	You have to be able to determine the byte size within the first 4 bytes of
	the serialized object (that is why we included a size indicator in the above example)

1.	When calculating the byteSize don't forget to include any bytes that
	a size indicator would occupy

1.	When reading the byteSize from a ByteBuffer your codec should
	not change the position in the ByteBuffer (that is why we used the utility
	function _CodecUtil.peekSize_

### Using BRecord

As an alternative to the above pattern where you implement your own codec you can use the _RecordCodec_ to 
serialize complex types.

Your class needs to extend _BRecord_ and the fields that should be serialized annotated with the _BField_. Also observe
that your class
1.  needs to implement the empty constructor
1.  the fields to serialize has to be primitive types (as defined above)

Given the example above (a complex type with an integer and string) you would define the following class:

```java
public class Container3 implements BRecord {

  @BField(2) public Integer f1;
  @BField(1) public String f2;

  public Container3(){}
  public Container3(int f1, String f2){this.f1 = f1; this.f2 = f2;}

}

```

Then using _RecordCodec_ you can do the following

```java

  public static void testContainer3(){
    Container3 c1 = new Container3(10, "ten");
    RecordCodec<Container3> codec = new RecordCodec<>(Container3.class);
    Container3 c2 = codec.deepCopy(c1);
    System.out.println("testContainer3: "+c2.f1+" "+c2.f2);
  }
```
As long as the objects you want to serialize only contain primitive fields it is 
recommended to use the BRecord/RecordCodec pattern.

## Comparing Objects on a Byte Level

The original (and still) main purpose of the Mellowtech Core library
was to offer functionality to sort and store objects on disc. In order
to do this we have to be able to compare objects. In many situations
the following would be sufficient

```java
"string".compareTo("string")
```

That is, you do your comparison on an object level. However, if you
millions of objects that have been serialized this strategy might impact 
significantly impact performance if you constantly have to create objects when
doing comparisons.

_BCodec_ allows object comparison on a byte level. All primitive codecs implements
byte level comparison. (The default behaviour of _BCodec_ is to is to create objects
and use the compareTo method when doing byte level comparison). 

For example, to compare to serialized strings without creating string objects you
 would do the following

```java
  public static void compareStrings(){
    StringCodec codec = new StringCodec();
	  ByteBuffer str1 = codec.to("a string");
	  ByteBuffer str2 = codec.to("a string");
	  System.out.println("compareStrings: "+codec.byteCompare(0, str1, 0, str2));
  }
```

In the above example we compare two string on a byte level that are stored in 2 different ByteBuffers.

A slightly more involved use case of how to use byte level comparison would be two compare
objects that are serialized to the same ByteBuffer. An example of this could look something like this

```java
  public static void compareInSameBuffer(){
    StringCodec codec = new StringCodec();
    ByteBuffer bb = ByteBuffer.wrap(new byte[codec.byteSize("a string 1")+codec.byteSize("a string")]);
    codec.to("a string 1", bb);
    codec.to("a string", bb);
    System.out.println("compareInSameBuffer: "+codec.byteCompare(0, codec.byteSize("a string 1"), bb));
  }
```

