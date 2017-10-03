package auyar.pms;
/**
 * Copyright 2017 Ahmet Uyar
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * sequential mergesort
 * classical recursive mergesort algorithm
 * sorting a long array
 * 
 * @author Ahmet Uyar
 */
public class MergeSortSeq {
    
	/**
	 * classical recursive merge sort algorithm
	 * first and last indexes are inclusive
	 * 
	 * @param array the data array to be sorted
	 * @param first start index of the sub array to be sorted
	 * @param last last index of the sub array to be sorted (inclusive)
	 */
    public static void mergeSort(long array[], int first, int last) {
        if (first == last) {
            return;
        }
        int middle = (first + last) / 2;
        mergeSort(array, middle + 1, last);
        mergeSort(array, first, middle);
        MergeSortUtil.merge(array, aux, first, middle + 1, last+1);
    }
    
    static long dd[] = {50, 70, 45, 30, 34, 78, 56, 10};
    static long aux[];

    public static void main(String[] args) {
        aux = new long[dd.length];
        mergeSort(dd, 0, dd.length - 1);
        print(dd);
    }
    
    public static void print(long[] dd) {
        for (int i = 0; i < dd.length; i++) {
            System.out.print(dd[i] + ", ");
        }
        System.out.println("");
    }
}
