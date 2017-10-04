package auyar.pms;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.ArrayList;
import java.util.Arrays;
 
/**
 * Recursive merge sort with Fork-Join Threads
 * 
 * We have made two assumptions to make the programming easier: 
 * 	Assumption 1: the number of elements to be sorted is divisible 
 * 		by the number of threads. So every block size is the same for all threads. 
 * 
 * 	Assumption 2: the number of threads is 2^k where k is an integer. 
 * 		So, always two blocks of equal size is merged.
 *    
 *  Thread recursion tree is always a full binary tree.
 *  Leaf threads perform sequential sort for their sub arrays.
 *  Non-leaf threads perform merge operations. 
 *  
 *  ThreadIDs: 
 *    The root thread id is 1. 
 *    The threads on the second level have ids: 2 and 3. 
 *    The threads on the third level have ids: 4, 5, 6, 7. 
 *    The threads on the fourth level have ids: 8, 9, 10, 11, 12, 13, 14, 15. 
 *    etc.
 *    
 *    If a thread has the id n: 
 *    the id of its left child is 2n
 *    the id of its right child is 2n+1
 *    
 *  If there are 8 cores in the system, then the thread tree will be a full binary tree with 15 nodes. 
 *  8 leaf threads with ids (8, 9, 10, 11, 12, 13, 14, 15) will execute the first sequential sort. 
 *  
 *  Thread id of the first leaf thread will always be equal to the number of cores or the number of leaf threads. 
 *  If a thread has an id that is equal or larger than the number of leaf threads, 
 *  then it is a leaf thread and needs to perform sequential search on its sub array. 
 *  Otherwise, it is a non-leaf thread and needs to perform merge operation. 
 * 
 * @author Ahmet uyar
 */
public class MergeSortWithForkJoinSTM1 extends RecursiveAction {
 
    private int threadID;
    private int start;
    private int length;
    private long array[];
    private long aux[];
    private int numberOfLeafThreads;
 
    public MergeSortWithForkJoinSTM1(int id, long array[], long aux[], int start, int length, int threads) {
        this.threadID = id;
        this.start = start;
        this.length = length;
        this.array = array;
        this.aux = aux;
        this.numberOfLeafThreads = threads;
        
//        String log = "Thread id: "+ id + "\tstart index: "+start+"\tlength: "+length;
//        logs.set(threadID, log);
    }
 
    // Each thread sorts its sub array using java.util.Array.sort method sequentially.
    protected void sortSequentially() {
        Arrays.sort(array, start, start+length);
    }
 
    @Override
    protected void compute() {
        if (threadID >= numberOfLeafThreads) {
            sortSequentially();
            return;
        }
 
        int blockSize = length / 2;
        MergeSortWithForkJoinSTM1 th1 = new MergeSortWithForkJoinSTM1(2*threadID, array, aux, start, blockSize, numberOfLeafThreads);
        MergeSortWithForkJoinSTM1 th2 = new MergeSortWithForkJoinSTM1(2*threadID+1, array, aux, start + blockSize, blockSize, numberOfLeafThreads);
        invokeAll(th1, th2);
        MergeSortUtil.merge(array, aux, th1.start, th2.start, th2.start+th2.length);
    }
     
    /**
     * a parallel sort method that can be called from any application 
     * @param array the array to be sorted. we assume the array is full. 
     * @param numberOfThreads user specifies the number of threads that will sort
     */
    public static void parallelMergeSort(long array[], int numberOfThreads) {
    	
    	MergeSortUtil.checkInput(array.length, numberOfThreads);
        long aux[] = new long[array.length];
        
        MergeSortWithForkJoinSTM1 fb = new MergeSortWithForkJoinSTM1(1, array, aux, 0, array.length, numberOfThreads);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(fb);
    }
    
 // keep the log messages in an ArrayList and print afterwards
//    static ArrayList<String> logs = new ArrayList<String>();
    
    public static void main(String[] args) {
    	
//      int numberOfLeafThreads = Runtime.getRuntime().availableProcessors();
        int numberOfLeafThreads = 16;
        int arraySize = 8000000;
        long array[] = new long[arraySize];
        long array2[] = new long[arraySize];
    	
    	// init arrays
    	MergeSortUtil.arrayInit(array, 20);
    	MergeSortUtil.arrayInit(array2, 30);
//    	initArrayList(numberOfLeafThreads);
    	
        long startTime = System.currentTimeMillis();
//        Arrays.sort(array2); // system sequential sort
        Arrays.parallelSort(array2); // system parallel sort
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("system sorting time: "+duration);
         
        startTime = System.currentTimeMillis();
        parallelMergeSort(array, numberOfLeafThreads);
        duration = System.currentTimeMillis() - startTime;
        System.out.println("sorting took " + duration + " milliseconds.");
         
        MergeSortUtil.isSorted(array);
        
     // print log messages from threads
//        for(String log: logs)
//        	System.out.println(log);
    }

//    public static void initArrayList(int numberOfLeafThreads) {
//    	for (int i=0; i<numberOfLeafThreads*2; i++) {
//			logs.add(null);
//		}
//    }    
}