package arrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArraysLeftRotation {

  public static void main(String[] args) {
    int[] arr = new int[]{1,2,3,4,5};

    System.out.println(Arrays.toString(arr));
    System.out.println(Arrays.toString(rotLeft(arr, 1)));
    System.out.println(Arrays.toString(rotLeft(arr, 2)));
    System.out.println(Arrays.toString(rotLeft(arr, 3)));
    System.out.println(Arrays.toString(rotLeft(arr, 4)));
    System.out.println(Arrays.toString(rotLeft(arr, 5)));
    System.out.println(Arrays.toString(rotLeft(arr, 6)));
  }

  /*                     1st            2nd            3rd
      [1,2,3,4,5] -> [2,3,4,5,1] -> [3,4,5,1,2] -> [4,5,1,2,3]
   */
  static int[] rotLeft(int[] a, int d) {
    int[] result = new int[a.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = a[(i + d) % result.length];
    }
    return result;
  }

}
