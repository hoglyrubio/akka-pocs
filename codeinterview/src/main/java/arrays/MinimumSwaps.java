package arrays;

import java.util.Arrays;

public class MinimumSwaps {
  /*
    https://www.hackerrank.com/challenges/minimum-swaps-2/problem?h_l=interview&playlist_slugs%5B%5D=interview-preparation-kit&playlist_slugs%5B%5D=arrays
    arr                     swap (indices)  v  p
    [7, 1, 3, 2, 4, 5, 6]   swap (0,3)      7-(0)=7, 1-(1)=0, 3-(2)=1, 2-(3)=-1, 4-(4)=0, 5-(5)=0, 6-(6)=0 (7,-1)
    [2, 1, 3, 7, 4, 5, 6]   swap (0,1)      2-(0)=2, 1-(1)=0, 3-(2)=1, 7-(3)=4 , 4-(4)=0, 5-(5)=0, 6-(6)=0 (2,0)
    [1, 2, 3, 7, 4, 5, 6]   swap (3,4)      1-(0)=1, 2-(1)=1,
    [1, 2, 3, 4, 7, 5, 6]   swap (4,5)
    [1, 2, 3, 4, 5, 7, 6]   swap (5,6)
    [1, 2, 3, 4, 5, 6, 7]

    [4,3,2,1] swap(0,2) 4-0=4*, 3-1=2, 2-2=0*, 1-3=-2  (4,0)
    [1,3,4,2] swap(1,2) 1-0=1, 3-1=2*, 4-2=2*, 2-3=-1  (2,2)
    [1,4,3,2] swap(1,3) 1-0=1, 4-1=3*, 3-2=1, 2-3=-1*  (3,-1)
    [1,2,3,4]
   */
  public static int minimumSwaps(int[] arr) {
    int counter = 0;

    for (int i = 0; i < arr.length; ) {
      if ((arr[i] - i) != 1) {
        int idx = i + 1;
        int dif = arr[idx] - idx;
        for (int j = i + 1; j < arr.length; j++) {
          if (dif > (arr[j] - j)) {
            idx = j;
          }
        }
        swap(arr, i, idx);
        System.out.println("swap(" + i + ", " + idx + ") " + Arrays.toString(arr));
        counter++;
      } else {
        i++;
      }
    }
    return counter;
  }

  private static void swap(int[] arr, int source, int target) {
    int value = arr[source];
    arr[source] = arr[target];
    arr[target] = value;
  }

  public static void main(String[] args) {
    //int array[] = new int[]{7, 1, 3, 2, 4, 5, 6};
    int array[] = new int[]{4,3,1,2};
    System.out.println("array: " + Arrays.toString(array));
    System.out.println("minimumSwaps: " + minimumSwaps(array));
    System.out.println("array: " + Arrays.toString(array));
  }

}
