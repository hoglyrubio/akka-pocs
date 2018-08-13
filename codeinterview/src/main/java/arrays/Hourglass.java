package arrays;

import java.util.ArrayList;
import java.util.List;

public class Hourglass {

  /*
    https://www.hackerrank.com/challenges/2d-array/problem?h_l=interview&playlist_slugs%5B%5D=interview-preparation-kit&playlist_slugs%5B%5D=arrays

    -9 -9 -9  1 1 1
     0 -9  0  4 3 2
    -9 -9 -9  1 2 3
     0  0  8  6 6 0
     0  0  0 -2 0 0
     0  0  1  2 4 0

    a b c
      d
    e f g
 */
  public static int hourglassSum(int[][] arr) {
    List<Integer> totals = new ArrayList<>();
    for(int i = 0; i < arr.length - 2; i++) {
      for (int j = 0; j < arr.length - 2; j++) {
        totals.add(arr[i][j] + arr[i][j+1] + arr[i][j+2] + arr[i+1][j+1] + arr[i+2][j] + arr[i+2][j+1] + arr[i+2][j+2]);
      }
    }
    return totals.stream().max(Integer::compareTo).orElse(0);
  }
}
