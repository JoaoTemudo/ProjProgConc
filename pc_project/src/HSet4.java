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

  public HSet4(int h_size) 
  {
    table = STM.newRef(STM.newTArray(h_size));
    size = STM.newRef(0); 
  }

  @Override
  public int capacity() 
  {
    return table.get().length();
  }

  @Override
  public int size() 
  {
    return size.get();
  }

  private Node<E> getEntry(E elem) 
  {
    return table.get().apply(hashMath(elem));
  }

  private int hashMath(E elem)
  {
    return Math.abs(elem.hashCode() % table.get().length());
  }

  @Override
  public boolean add(E elem) 
  {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() ->{
      if(contains(elem)){
        return false;
      }
      Node<E> curNode = getEntry(elem);
      Node<E> newNode = new Node<E>();
      newNode.value = elem;
      if(curNode != null){
        curNode.prev.set(newNode);
      }
      newNode.next.set(curNode);
      TArray.View<Node<E>> nTable = table.get();
      nTable.update(hashMath(elem), newNode);
      STM.increment(size, 1);
      return true;
    });
  }
  
  @Override
  public boolean remove(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    return STM.atomic(() ->{
      Node<E> curNode = getEntry(elem);
      while(curNode != null){
        if(elem.equals(curNode.value)){
          Node<E> prevNode = curNode.prev.get();
          Node<E> nextNode = curNode.next.get();
          
          if(nextNode != null){
            nextNode.prev.set(prevNode);
          }
          if(prevNode != null){
            prevNode.next.set(nextNode);           
          }else{
            TArray.View<Node<E>> nTable = table.get();
            nTable.update(hashMath(elem), nextNode);
          }
          
          STM.increment(size, -1);
          return true;
        }
        curNode = curNode.next.get();
      }
      return false;
    });
  }

  @Override
  public boolean contains(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }

    return STM.atomic(() ->{
      Node<E> curNode = getEntry(elem);
      while(curNode != null){
        if(elem.equals(curNode.value)){
          return true;
        }
        curNode = curNode.next.get();
      }
      return false;
    });
  }

  @Override
  public void waitFor(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    STM.atomic(() ->{
      if(!contains(elem))
      {
        STM.retry();
      }
    });
  }

  @Override
  public void rehash() {
    STM.atomic(() ->{
      TArray.View<Node<E>> oldTable = table.get();
      Node<E> curNode;
      table.set(STM.newTArray(oldTable.length()*2));
      size.set(0);
      for(int i = 0; i < oldTable.length(); i++)
      {
        curNode = oldTable.apply(i);
        while(curNode != null)
        {
          add(curNode.value);
          curNode = curNode.next.get();
        }
      }
    });
  }
}
