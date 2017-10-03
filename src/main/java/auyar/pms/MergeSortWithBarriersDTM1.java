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
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Iterative parallel merge sort with double thread merging
 *  
 * we make two assumptions to make programming easier: 
 * Assumption 1: the number of elements to be sorted is divisible 
 * by the number of threads. So every array sub block is the same for all threads. 
 * 
 * Assumption 2: the number of threads is 2^k, where k is an integer. 
 * So, always two blocks of equal size is merged.
 * 
 * the array is divided into numberOfThread sub blocks. 
 * each thread sorts its range sequentially
 * then each merge operation is handled by two threads
 * one thread merges the first half (smaller ones), 
 * the other thread merges the second half (larger ones)
 * 
 * If there are 16 blocks to be merged, 16 threads perform merge operations
 *   First two threads merge first two blocks, 
 *   next two thread merge next two blocks, and so on. 
 * In the next iteration, there are 8 blocks to merge.
 *   This time, 8 threads merge these blocks. 
 *   First two threads merge first two blocks, 
 *   next two thread merge next two blocks, and so on. 
 * In the last iteration, there are two threads to merge
 *   First two threads merge these two blocks,
 *   
 * After every merge iteration, the size of each block is doubled and 
 * the number of blocks is reduced by half. 
 * 
 * Synchronization of threads:
 *   threads wait to synchronize at two points in each iteration
 *     a) after merging two sorted subarrays
 *     b) after copying back to the original array 
 * 
 * @author Ahmet Uyar
 */
public class MergeSortWithBarriersDTM1 extends Thread {
    private int threadID;
    private CyclicBarrier barrier;
    private long array[];
    private long aux[];
    private int numberOfThreads;
 
    public MergeSortWithBarriersDTM1(int threadID, CyclicBarrier barrier, long array[], long aux[], int numberOfThreads) {
        super("thread " + threadID);
        this.threadID = threadID;
        this.barrier = barrier;
        this.array = array;
        this.aux = aux;
        this.numberOfThreads = numberOfThreads;
    }    

    @Override
    public void run() {
        try {
        	// each thread calculates its blockSize and first/last indexes of that block
        	// then perform sequential search on that block
            int blockSize = array.length / numberOfThreads;
            int first = threadID * blockSize;
            int last = first + blockSize;
            Arrays.sort(array, first, last);
            
            // before merge operations to start, all threads need to finish sequential search of their block
            barrier.await();
            
            // activeThreads variable keeps count of threads that will perform merging in every iteration
            // threads with threadID values lower than activeThreads will perform merging in that iteration
            int activeThreads = numberOfThreads;
            while(activeThreads>1){
            	// threads with even threadIDs merge lower half of the blocks to be sorted
                if(threadID<activeThreads && threadID %2 == 0){
                    int start = threadID*blockSize;
                    int second = start+blockSize;
                    int third = second+blockSize;
                    MergeSortUtil.mergeMins(array, aux, start, second, third);
                    barrier.await();
                    // copy back the merged block to the original array
                    System.arraycopy(aux, start, array, start, blockSize);
                    
              	// threads with odd threadIDs merge higher half of the blocks to be sorted
                }else if(threadID<activeThreads && threadID %2 != 0){
                	int start = (threadID-1)*blockSize;
                    int second = start+blockSize;
                    int third = second+blockSize;
                    MergeSortUtil.mergeMaxes(array, aux, start, second, third);
                    barrier.await();
                    // copy back the merged block to the original array
                    System.arraycopy(aux, second, array, second, blockSize);
                }else{
                	// idle looping threads wait to synchronize other threads to finish merge operation
                    barrier.await();
                }
                // blockSize is doubled after every merge operation
                blockSize *= 2;
                // number of active threads is reduced by half after every merge operation
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
    	
    	MergeSortWithBarriersDTM1 threads[] = new MergeSortWithBarriersDTM1[numberOfThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MergeSortWithBarriersDTM1(i, barrier, array, aux, numberOfThreads);
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
        
        int numberOfThreads = 4;
        int arraySize = 8000000;
        long array[] = new long[arraySize];
        long array2[] = new long[arraySize];
        
        MergeSortUtil.arrayInit(array, 20);
        MergeSortUtil.arrayInit(array2, 30);
        
        long startTime = System.currentTimeMillis();
//        Arrays.sort(array2); // system sequential sort
        Arrays.parallelSort(array2); // system parallel sort
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
