package auyar.pms;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.Arrays;
import java.util.ArrayList;
 
/**
 * Recursive merge sort with Fork-Join Threads
 * 
 * We removed the two restrictions from the earlier version: 
 *    MergeSortWithForkJoinSTM1.java
 *    
 * The number of elements to be sorted does not need to be divisible by the number of leaf threads. 
 * the number of threads may be any positive number. It does not have to be a power of 2.
 * 
 * Recursion Tree: 
 * In this case, thread recursion tree is a complete binary tree, not a full binary tree.
 * Still leaf threads perform sequential sort for their sub arrays.
 * Non-leaf threads perform merge operations. 
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
 *  If there are 6 cores in the system, then the thread tree will be a complete binary tree with 11 nodes. 
 *  6 leaf threads with ids (6, 7, 8, 9, 10, 11) will execute the first sequential sort. 
 *  First 5 threads (1, 2, 3, 4, 5) will perform merge operations. 
 *  
 *  Thread id of the first leaf thread will always be equal to the number of cores or the number of leaf threads. 
 *  If a thread has an id that is equal or larger than the number of leaf threads, 
 *  then it is a leaf thread and needs to perform sequential search on its sub array. 
 *  Otherwise, it is a non-leaf thread and needs to perform merge operation. 
 *  
 *  In this case, leaf threads are either in the last level or the level before the last level. 
 *  First sub array is assigned to the first thread of the last level. 
 *  Second sub array is assigned to the second thread of the last level. 
 *  When the threads in last level is finished, 
 *  the threads from the previous level are assigned from left to right order. 
 * 
 * @author Ahmet uyar
 */
public class MergeSortWithForkJoinSTM2 extends RecursiveAction {
 
    private int threadID;
    private int start; 
    private int length;
    private long array[];
    private long aux[];
    private int numberOfLeafThreads;
 
    public MergeSortWithForkJoinSTM2(int id, long array[], long aux[], int threads) {
        this.threadID = id;
        this.array = array;
        this.aux = aux;
        this.numberOfLeafThreads = threads;
    }
 
    /**
     * Each thread sorts its sub array using java.util.Array.sort method sequentially.
     * The tricky part is calculating the subarray a thread will sort
     */
    
    protected void sortSequentially() {
    	int blockSize = array.length/numberOfLeafThreads; // the size of the sub array that will be sorted sequentially
    	int firstLeafNode = numberOfLeafThreads; 
    	int lastNodeID = numberOfLeafThreads*2-1; // last node of the thread tree
    	int treeHeight = (int)(Math.log(lastNodeID)/Math.log(2));
    	int firstNodeOfLastLevel = (int)Math.pow(2, treeHeight);
    	int nodesInLastLevel = lastNodeID - firstNodeOfLastLevel + 1;

    	// if the thread is in the last level
    	if(threadID >= firstNodeOfLastLevel)
    		start = (threadID-firstNodeOfLastLevel)*blockSize;
    	
    	// if the thread is in the previous level
    	else
    		start = (nodesInLastLevel + (threadID-firstLeafNode))*blockSize;
    	
    	length = blockSize;

    	// if this is the thread that will sort the last sub array
    	// it is the last node in the last level or the previous level
    	// if we add 1 to the threadID, we get a number that is a power of 2
    	if( MergeSortUtil.checkPowerOfTwo(threadID+1) )
    		length = array.length - start;
    	
//    	String log = "Thread id: "+ threadID + "   start index: "+start+" \tlength: "+length;
//    	logs.set(threadID, log);
    	
        Arrays.sort(array, start, start+length);
    }
 
    @Override
    protected void compute() {
        if (threadID >= numberOfLeafThreads) {
            sortSequentially();
            return;
        }
 
        MergeSortWithForkJoinSTM2 th1 = new MergeSortWithForkJoinSTM2(2*threadID, array, aux, numberOfLeafThreads);
        MergeSortWithForkJoinSTM2 th2 = new MergeSortWithForkJoinSTM2(2*threadID+1, array, aux, numberOfLeafThreads);
        invokeAll(th1, th2);
        MergeSortUtil.merge(array, aux, th1.start, th2.start, th2.start+th2.length);
        start = th1.start;
        length = th1.length + th2.length;
        
//        String log = "Thread id: "+ threadID + "   start index: "+start+" \tlength: "+length;
//        logs.set(threadID, log);
    }
    
    /**
     * a parallel sort method that can be called from any application 
     * @param array the array to be sorted. we assume the array is full. 
     * @param numberOfThreads user specifies the number of threads that will sort
     */
    public static void parallelMergeSort(long array[], int numberOfThreads) {
    	
        long aux[] = new long[array.length];
        
        MergeSortWithForkJoinSTM2 fb = new MergeSortWithForkJoinSTM2(1, array, aux, numberOfThreads);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(fb);
    }
    
    public static void main(String[] args) {
    	
//      int numberOfLeafThreads = Runtime.getRuntime().availableProcessors();
        int numberOfLeafThreads = 17;
        int arraySize = 8000003;
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
    }
}