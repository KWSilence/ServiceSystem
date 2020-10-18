package smo_system;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class Buffer
{
  private final int capacity;
  private final ArrayList<Request> list;
  private int takeIndex;

  private final NumberFormat formatter = new DecimalFormat("#0.000");

  public Buffer(int capacity)
  {
    this.capacity = capacity;
    this.list = new ArrayList<>();
    this.takeIndex = 0;
  }

  public boolean isEmpty()
  {
    return list.isEmpty();
  }

  public boolean isFull()
  {
    return (list.size() == capacity);
  }

  public int getTakeIndex()
  {
    return takeIndex;
  }

  public int getSize()
  {
    return list.size();
  }

  public ArrayList<Request> getList()
  {
    return list;
  }

  public int getCapacity()
  {
    return capacity;
  }

  public void printList()
  {
    if (!isEmpty())
    {
      System.out.println("---------------BUFFER---------------");
      int index = 0;
      for (Request r : list)
      {
        System.out.println((++index) + " #" + (r.getSourceNumber() + 1) + "." + (r.getNumber() + 1) + " created " +
                           formatter.format(r.getTime()));
      }
      for (int i = index + 1; i <= capacity; i++)
      {
        System.out.println(i + " EMPTY");
      }
      System.out.println("____________________________________");
    }

  }

  public void putRequest(Request request)
  {
    list.add(request);
  }

  public Request getRequest()
  {
    if (isEmpty())
    {
      return null;
    }
    takeIndex = getPriorityIndex();
    Request request = list.get(takeIndex);
    list.remove(takeIndex);
    return request;
  }

  private int getPriorityIndex()
  {
    int priority = list.get(0).getSourceNumber();
    int index = 0;
    for (int i = 0; i < list.size(); i++)
    {
      int current = list.get(i).getSourceNumber();
      if (priority > current)
      {
        priority = current;
        index = i;
      }
    }
    return index;
  }

}
