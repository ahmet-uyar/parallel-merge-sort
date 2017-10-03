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
 * Utility methods for parallel merge sort algorithm
 * 
 * merge method is used to merge two sorted subarrays into one by a single thread
 * mergeMins method is used to merge the first half of two sorted subarrays
 * mergeMaxes method is used to merge the second half of two sorted subarrays
 * 
 * mergeMins and mergeMaxes are expected to be executed by two different threads simultaneously
 * Consequently, merge operation is handled by two concurrent threads in parallel
 * 
 * isSorted method checks whether the given array is sorted in increasing order
 * 
 * @author Ahmet Uyar
 */

public class MergeSortUtil {

    /**
     * merge two sorted subarrays
     * 
     * @param start1 the start index of the first sorted block
     * @param start2 the start index of the second sorted block
     * @param last the first element after the second sorted block
     */
    public static void merge(long d1[], long aux[], int start1, int start2, int last){
        int index1 = start1;
        int index2 = start2;
        int index3 = start1;
        while(index1<start2 && index2<last){
            if(d1[index1] < d1[index2]){
                aux[index3] = d1[index1];
                index1++;
                index3++;
            }else{
                aux[index3] = d1[index2];
                index2++;
                index3++;
            }
        }
        
        // if there are some elements left in the first sorted subarray,
        // copy them to auxiliary array
        while(index1<start2){
            aux[index3++] = d1[index1++];
        }
        
        // if there are some elements left in the second sorted subarray,
        // copy them to auxiliary array
        while(index2<last){
            aux[index3++] = d1[index2++];
        }
        
        //copy back from the auxiliary array to the original data array
        System.arraycopy(aux, start1, d1, start1, last-start1);
    }

    /**
     * merge half of the two sorted sub arrays
     * merge smaller values in the first half of the auxiliary array
     * this method is based on the above more general merge method
     * It only merges half of the total number of elements to be merged
     * 
     * @param start1 the start index of the first sorted block
     * @param start2 the start index of the second sorted block
     * @param last the first element after the second sorted block
     * 
     * returns the number of merged elements
     */
    public static int mergeMins(long d1[], long aux[], int start1, int start2, int last){
        int index1 = start1;
        int index2 = start2;
        int index3 = start1;
        int elementsToMerge = (last - start1)/2;
        int counter = 0;
        
        while(index1<start2 && index2<last  && counter<elementsToMerge){
            if(d1[index1] < d1[index2]){
                aux[index3] = d1[index1];
                index1++;
                index3++;
            }else{
                aux[index3] = d1[index2];
                index2++;
                index3++;
            }
            counter++;
        }
        
        // if no element left in second subarray, get elements from first sorted subarray,
        // copy them to auxiliary array directly
        while(index1<start2  && counter<elementsToMerge){
            aux[index3++] = d1[index1++];
            counter++;
        }
        
        // if no element left in the first subarray, get elements from second sorted subarray,
        // copy them to auxiliary array directly
        while(index2<last  && counter<elementsToMerge){
            aux[index3++] = d1[index2++];
            counter++;
        }
        
        return (index3-start1);
    }

    /**
     * merge half of the two sorted sub arrays
     * merge larger values and put in the second half of the auxiliary array
     * if the total number of elements is an odd number, 
     * merge one more than the half of the elements to be merged
     * 
     * the method is based on the above more general merge method
     * it starts merging maximums of two sorted subarrays
     * it continues to merge the current maximum of two sorted subarrays
     * until required number of elements are merged
     * 
     * @param start1 the start index of the first sorted block
     * @param start2 the start index of the second sorted block
     * @param last the first element after the second sorted block
     * 
     * returns the number of merged elements
     */
    public static int mergeMaxes(long d1[], long aux[], int start1, int start2, int last){
        int index1 = start2-1;
        int index2 = last-1;
        int index3 = last-1;
        int elementsToMerge = (int)Math.ceil( (last - start1)/2.0 );
        int counter = 0;
        
        while(index1>=start1 && index2>=start2  && counter<elementsToMerge){
            if(d1[index1] > d1[index2]){
                aux[index3] = d1[index1];
                index1--;
                index3--;
            }else{
                aux[index3] = d1[index2];
                index2--;
                index3--;
            }
            counter++;
        }
        
        // if no element left in second subarray, get elements from first sorted subarray,
        // copy them to auxiliary array directly
        while(index1>=start1  && counter<elementsToMerge){
            aux[index3--] = d1[index1--];
            counter++;
        }
        
        // if no element left in the first subarray, get elements from second sorted subarray,
        // copy them to auxiliary array directly
        while(index2>=start2  && counter<elementsToMerge){
            aux[index3--] = d1[index2--];
            counter++;
        }
        
        return (last - index3 -1);
    }

    /**
     * check whether the given array is sorted.
     * If not, print an error message for each unsorted pair 
     * 
     * @param array the array to be checked whether it is sorted in increasing order
     */
    public static void isSorted(long array[]) {
    	
    	boolean sorted = true;
        for (int i = 0; i < array.length-1; i++) {
            if(array[i] > array[i+1]){
                System.out.println("not sorted");
                System.out.println(i+": "+array[i]);
                System.out.println((i+1)+": "+array[i+1]);
                sorted = false;
            }
        }
        
        if(sorted)
        	System.out.println("array is sorted.");
    }    
    
    /**
     * initialize a given long array with random long values
     * 
     * @param array array to be initialized
     * @param seed seeding the randomizer
     */
    public static void arrayInit(long array[], int seed) {
        java.util.Random r = new java.util.Random(seed);
        for (int j = 0; j < array.length; j++) {
            array[j] = r.nextLong();
        }
    }
    
    /**
     * return true if the given number is a power of two positive integer
     * Wikipedia source for the algorithm: 
     * http://en.wikipedia.org/wiki/Power_of_two#Fast_algorithm_to_check_if_a_positive_number_is_a_power_of_two
     * 
     * @param number to check whether it is a power of two
     * @return true if the given number is a power of two, false otherwise
     */
    public static boolean checkPowerOfTwo(int number) {
    	    return ((number & (number - 1)) == 0);
  	}
    
    /**
     * checks whether two assumptions hold: 
     * Assumption 1: the number of elements to be sorted is divisible by the number of threads. 
     * Assumption 2: the number of threads is 2^k where k is an integer. 
     * 
     * If any of these two assumptions does not hold, quit the program
     * 
     * @param arraySize the size of the input array
     * @param numberOfThreads the number of threads that will perform parallel sort
     */
    public static void checkInput(int arraySize, int numberOfThreads) {
    	if(!MergeSortUtil.checkPowerOfTwo(numberOfThreads)) {
    		System.out.println("Number of threads must be a power of two.");
    		System.out.println(numberOfThreads + " is not a power of two.");
        	System.exit(0);
    	}
    	
    	if((arraySize % numberOfThreads) != 0) {
    		System.out.println("number of elements must be divisible by the number of threads.");
    		System.out.println("number of elements: " + arraySize);
    		System.out.println("number of threads: " + numberOfThreads);
        	System.exit(0);
    	}
    }    
    
    public static void main(String[] args) throws Exception {
    	
        long array[] = {10, 20, 35, 45, 65, 75, 85, 95, 30, 60, 115};
        long aux[] = new long[array.length];

        MergeSortUtil.mergeMins(array, aux, 0, 8, array.length);
        MergeSortUtil.mergeMaxes(array, aux, 0, 8, array.length);
        
        // print arrays
        System.out.println("original array");
        for(long n: array)
        	System.out.print(n+", ");

        System.out.println();
        System.out.println("auxiliary array");
        for(long n: aux)
        	System.out.print(n+", ");
    }    
}
