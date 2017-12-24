package org.mellowtech.core.collections;

import org.mellowtech.core.codec.BCodec;
import org.mellowtech.core.codec.Codecs;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Martin Svensson {@literal <msvens@gmail.com>}
 * @since 4.0.0
 * @param <A> key type
 * @param <B> value type
 * @param <T> self type
 */
abstract class CollectionBuilder <A,B,T extends CollectionBuilder<A,B,T>> {

  static class DirAndName{
    final String name;
    final Path dir;
    public DirAndName(Path p){
      dir = p.getParent();
      name = p.getFileName().toString();
    }
  }

  BCodec<A> keyCodec = null;
  BCodec<B> valueCodec = null;
  Path filePath = null;

  public T copyBuilder(CollectionBuilder<A,B,?> other){
    this.keyCodec = other.keyCodec;
    this.valueCodec = other.valueCodec;
    this.filePath = other.filePath;
    return (T) this;
  }

  public BCodec<A> keyCodec(){
    return keyCodec;
  }

  public T keyCodec(BCodec<A> keyCodec){
    this.keyCodec = keyCodec;
    return (T) this;
  }

  public T keyCodec(Class<A> keyClass){
    this.keyCodec = Codecs.fromClass(keyClass);
    return (T) this;
  }

  public BCodec<B> valueCodec(){
    return valueCodec;
  }

  public T valueCodec(BCodec<B> valueCodec){
    this.valueCodec = valueCodec;
    return (T) this;
  }

  public T valueCodec(Class<B> valueClass){
    this.valueCodec = Codecs.fromClass(valueClass);
    return (T) this;
  }

  public T filePath(Path filePath){
    this.filePath = filePath;
    return (T) this;
  }

  public Path filePath(){
    return this.filePath;
  }

  public DirAndName filePathSplit(){
    return new DirAndName(filePath);
  }


  public void checkParameters(){
    Objects.requireNonNull(keyCodec, "key codec is null");
    Objects.requireNonNull(valueCodec, "value codec is null");
    Objects.requireNonNull(filePath, "file path is null");
  }



}
