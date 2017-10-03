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
 * This is an improved version of MergeSortWithBarriersSTM1.java class
 * 
 * It removes two restrictions from that algorithm: 
 * 	 the number of threads may be any positive number. It does not have to be a power of 2.
 *   the number of elements does not need to be divisible by the number of threads.
 *  
 * When those two restrictions are removed: 
 *   when there are odd number of blocks to be sorted on an iteration, 
 *     the last block is not merged. All other consecutive block pairs are merged.
 *   All block sizes are the same except the last block. 
 *     Initially it may be a little longer but after some iterations, it is usually smaller.  
 * 
 * The algorithm is the same other then these two improvements.
 * 
 * Thread ids: 
 *  each thread has a unique threadID. 
 *  threads are assigned threadIDs starting from zero and increasing by one. 
 *     threadID of first thread 0
 *     threadID of second thread 1
 *     threadID of third thread 2
 *     etc. 
 *      
 * @author Ahmet Uyar
 */
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
 
public class MergeSortWithBarriersSTM2 extends Thread {
 
    private int threadID;
    private CyclicBarrier barrier;
    private long array[];
    private long aux[];
    private int numberOfThreads;
 
    public MergeSortWithBarriersSTM2(int threadID, CyclicBarrier barrier, long array[], long aux[], int numberOfThreads) {
        super("thread " + threadID);
        this.threadID = threadID;
        this.barrier = barrier;
        this.array = array;
        this.aux = aux;
        this.numberOfThreads = numberOfThreads;
    }
 
    public void run() {
        try {
        	// all blocks are equal in size except the last one
        	// last one initially may be a little larger
            int blockSize = array.length / numberOfThreads;
            int first = threadID * blockSize;
            int last = first + blockSize;
            if(threadID == numberOfThreads-1)
                last = array.length;
            
            // each thread sorts its subarray and wait others at the barrier
            Arrays.sort(array, first, last);
            barrier.await();
             
            int numberOfBlocks = numberOfThreads;
            int activeThreads = numberOfBlocks/2;
             
            while(activeThreads>0){
                if(threadID < activeThreads){
                    int start = 2 * threadID * blockSize;
                    int second = start+blockSize;
                    int third = second+blockSize;
                    // if there are even number of blocks, 
                    // only then the last block is merged
                    if(numberOfBlocks%2==0 && threadID == activeThreads-1)
                        third = array.length;
                    MergeSortUtil.merge(array, aux, start, second, third);
                }
                blockSize *= 2;
                // numberOfBlocks ceiled up, since if there are odd numberOfBlocks,
                // all consecutive block pairs are merged, but the last one is not merged
                // if there are 7 blocks to merge, 6 of them are merged into 3. 
                // last one stayed the same. So in the next iteration, we have (3+1=4) blocks.  
                numberOfBlocks = (int)Math.ceil(numberOfBlocks/2.0);
                
                // activeThreads must be calculated by dividing numberOfBlocks by 2, 
                // not dividing activeThreads by 2.
                activeThreads = numberOfBlocks/2;
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
    	
        long aux[] = new long[array.length];
        
    	CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
    	
        MergeSortWithBarriersSTM2 threads[] = new MergeSortWithBarriersSTM2[numberOfThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MergeSortWithBarriersSTM2(i, barrier, array, aux, numberOfThreads);
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
         
        int numberOfThreads = 15;
        int arraySize = 8000003;
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