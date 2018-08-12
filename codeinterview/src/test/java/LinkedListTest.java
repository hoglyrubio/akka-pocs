import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LinkedListTest {

  @Test
  public void shouldReverse() {
    LinkedList list = new LinkedList("A", new LinkedList("B", new LinkedList("C", null)));
    LinkedList reversed = LinkedList.reverse(list);
    assertThat(reversed.toString(), is(equalTo("C->B->A->")));
  }

}
