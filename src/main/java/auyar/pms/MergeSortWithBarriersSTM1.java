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
 * Parallel iterative merge sort with CyclicBarrier
 * 
 * The number of elements are divided by the number of threads (cores).
 * First, each thread sorts its range sequentially by using Arrays.sort method.
 * 
 * Then half of the threads perform merge operation on consecutive sorted sub arrays.
 * In every merge iteration, the number of merging threads reduced by half.
 * Initially, if there are 16 threads: 
 *    all 16 threads perform sorting with Arrays.sort method
 *    Iteration 1: first 8 threads perform merge operation
 *    Iteration 2: first 4 threads perform merge operation
 *    Iteration 3: first 2 threads perform merge operation
 *    Iteration 4: first 1 threads perform merge operation
 * 
 * Thread ids: 
 *  each thread has a unique threadID. 
 *  threads are assigned threadIDs starting from zero and increasing by one. 
 *     threadID of first thread is 0
 *     threadID of second thread is 1
 *     threadID of third thread is 2
 *     etc. 
 * 
 * We have made two assumptions to make the programming easier: 
 * 	Assumption 1: the number of elements to be sorted is divisible 
 * 		by the number of threads. So every block size is the same for all threads. 
 * 
 * 	Assumption 2: the number of threads is 2^k where k is an integer. 
 * 		So, always two blocks of equal size is merged.
 * 
 * @author Ahmet Uyar
 */
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MergeSortWithBarriersSTM1 extends Thread {

    private int threadID;
    private CyclicBarrier barrier;
    private long array[];
    private long aux[];
    private int numberOfThreads;

    public MergeSortWithBarriersSTM1(int threadID, CyclicBarrier barrier, long array[], long aux[], int numberOfThreads) {
        super("thread " + threadID);
        this.threadID = threadID;
        this.barrier = barrier;
        this.array = array;
        this.aux = aux;
        this.numberOfThreads = numberOfThreads;
    }

    public void run() {
        try {
            int blockSize = array.length/numberOfThreads;
            int first = threadID * blockSize;
            int last = first + blockSize;

            // each thread sort its sub array and all waits at the barrier
            Arrays.sort(array, first, last);
            barrier.await();
            
            int activeThreads = numberOfThreads/2;
            
            while(activeThreads>0){
                if(threadID < activeThreads){
                    int start = 2 * threadID * blockSize;
                    int second = start+blockSize;
                    int third = second+blockSize;
                    MergeSortUtil.merge(array, aux, start, second, third);
                }
                blockSize *= 2;
                activeThreads = activeThreads/2;
                barrier.await();
            }
        } catch (InterruptedException ex) {
            System.out.println("exception error message: " + ex.getMessage());
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            System.out.println("exception error message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
        
    /**
     * a parallel sort method that can be called from any application 
     * @param array the array to be sorted. we assume the array is full. 
     * @param numberOfThreads user specifies the number of threads that will sort
     */
    public static void parallelMergeSort(long array[], int numberOfThreads) {
    	
    	MergeSortUtil.checkInput(array.length, numberOfThreads);
        long aux[] = new long[array.length];
        
    	CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
    	
        MergeSortWithBarriersSTM1 threads[] = new MergeSortWithBarriersSTM1[numberOfThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MergeSortWithBarriersSTM1(i, barrier, array, aux, numberOfThreads);
            threads[i].start();
        }
        
        // main thread waits for the first thread to finish. 
        // it could have waited any other thread. all finish simultaneously
        try {
        	threads[0].join();
        }catch(InterruptedException ie) {
        	ie.printStackTrace();
        }
    }
    
    public static void main(String args[]){
    	
        int numberOfThreads = 16;
        int arraySize = 8000000;
        long array[] = new long[arraySize];
        long array2[] = new long[arraySize];
        
        MergeSortUtil.arrayInit(array, 20);
        MergeSortUtil.arrayInit(array2, 30);
        
        long startTime = System.currentTimeMillis();
//        Arrays.sort(array2); // sequential sort
        Arrays.parallelSort(array2); // parallel sort
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("system sorting time: "+duration);

        // parallel sorting
        startTime = System.currentTimeMillis();
        parallelMergeSort(array, numberOfThreads);
        duration = System.currentTimeMillis() - startTime;
        
        System.out.println("parallel sorting time: "+duration);
        MergeSortUtil.isSorted(array);
        System.out.println("main thraed has finished. ");
    }
}
