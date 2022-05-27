import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HSet1<E> implements IHSet<E>{

  private LinkedList<E>[] table;
  private int size;

  private final ReentrantLock lock= new ReentrantLock();
  private final Condition hasElem= lock.newCondition();

  /**
   * Constructor.
   * @param ht_size Initial size for internal hash table.
   */
  public HSet1(int ht_size) {
    table = createTable(ht_size);
    size = 0;
  }
  
  
  // Auxiliary method to return the list where
  // an element should be stored.
  private LinkedList<E> getEntry(E elem) {
    return table[Math.abs(elem.hashCode() % table.length)];
  }

  // Auxiliary method to create the hash table.
  private LinkedList<E>[] createTable(int ht_size) {
    @SuppressWarnings("unchecked")
    LinkedList<E>[] t = (LinkedList<E>[]) new LinkedList[ht_size];
    for (int i = 0; i < t.length; i++) {
      t[i] = new LinkedList<>();
    }
    return t;
  }
 
  @Override
  public int capacity() {
    return table.length;
  }
  
  @Override
  public int size() {
    lock.lock();
    try{
      return size;
    } finally{
      if(lock.isHeldByCurrentThread())
        lock.unlock();
    }
  }

  @Override
  public boolean add(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    lock.lock();
    try{
      LinkedList<E> list = getEntry(elem);
      boolean r = ! list.contains(elem);
      if (r) {
        list.addFirst(elem);
        hasElem.signalAll(); // there may threads waiting in waitEleme
        size++;
      }
      return r;
    } finally{
      if(lock.isHeldByCurrentThread())
        lock.unlock();
    }
  }

  @Override
  public boolean remove(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    lock.lock();
    try{
      boolean r = getEntry(elem).remove(elem);
      if (r) {
        size--;
      }
      return r;
    } finally{
      if(lock.isHeldByCurrentThread())
        lock.unlock();
    }
  }

  @Override
  public boolean contains(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    lock.lock();
    try {
      return getEntry(elem).contains(elem);
    } finally {
      if(lock.isHeldByCurrentThread())
        lock.unlock();
    }
  }

  @Override
  public void waitFor(E elem) {
    if (elem == null) {
      throw new IllegalArgumentException();
    }
    lock.lock();
    try{
      while (! getEntry(elem).contains(elem)) {
        try {
            hasElem.await();
        } catch(InterruptedException e) { 
          // Ignore interrupts
        }
      }
    } finally{
      if (lock.isHeldByCurrentThread())
        lock.unlock();
    }
  }
  
  @Override
  public void rehash() {
    lock.lock();
    try {
      LinkedList<E>[] oldTable = table;
      table = createTable(2 * oldTable.length);
      for (LinkedList<E> list : oldTable) {
        for (E elem : list ) {
          getEntry(elem).add(elem);
        } }
    } finally {
      if(lock.isHeldByCurrentThread())
        lock.unlock();
    }
  }
}

