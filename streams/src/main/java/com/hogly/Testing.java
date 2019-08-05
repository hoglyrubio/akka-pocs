package com.hogly;

import java.util.Arrays;
import java.util.List;

public class Testing {

  public static void main(String[] args) {

    Integer[] array = new Integer[]{1, 2, 3};

    List<Integer> list1 = Arrays.asList(array);
    array[1] = 10;
    System.out.println(Arrays.asList(array));
    System.out.println(list1);
    list1.remove(2);
  }

}
