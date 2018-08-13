public class LinkedList {

  public String value;
  public LinkedList next;

  public LinkedList(String value, LinkedList next) {
    this.value = value;
    this.next = next;
  }

  @Override
  public String toString() {
    LinkedList current = this;
    StringBuilder sb = new StringBuilder();
    while (current != null) {
      sb.append(current.value);
      sb.append("->");
      current = current.next;
    }
    return sb.toString();
  }

  /*c
        1 -> 2 -> 3 -> 4 -> null
        4 -> 3 -> 2 -> 1 -> null
   */
  public static LinkedList reverse(LinkedList list) {
    LinkedList current = list;
    LinkedList newCurrent = null;
    while (current != null) {
      LinkedList oldNext = current.next;
      current.next = newCurrent;
      newCurrent = current;
      current = oldNext;
    }
    return newCurrent;
  }

}
