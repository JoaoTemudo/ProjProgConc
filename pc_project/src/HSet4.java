import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

public class HSet4<E> implements IHSet<E>{

  private static class Node<T> {
    T value;
    Ref.View<Node<T>> prev = STM.newRef(null);
    Ref.View<Node<T>> next = STM.newRef(null);
  }

  private final Ref.View<TArray.View<Node<E>>> table;
  private final Ref.View<Integer> size;

  public HSet4(int h_size) {
    table = STM.newRef(STM.newTArray(h_size));
    size = STM.newRef(0); 
  }

  @Override
  public int capacity() {
    return table.get().length();
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean add(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    // TODO
    throw new Error("not implemented");
  }

  @Override
  public boolean remove(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    // TODO
    throw new Error("not implemented");
  }

  @Override
  public boolean contains(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    // TODO
    throw new Error("not implemented");
  }

  @Override
  public void waitFor(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    // TODO
    throw new Error("not implemented");
  }

  @Override
  public void rehash() {
    // TODO
    throw new Error("not implemented");
  }

}
