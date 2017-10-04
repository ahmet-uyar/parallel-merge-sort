# Parallel Merge Sort With Double Merging

Sorting is one of the most important problems in computer science. There are many sequential sorting algorithms. However, many of those sequential sorting algorithms are not suitable to parallelize. 

Merge sort is one of the sorting algorithms that is suitable for parallelization. Here is the steps of a general parallel merge sort algorithm: 
1. divide the elements to be sorted into the number of cores in the system
1. each core sorts its assigned sub array independently. (they may use any of the sequential sorting algorithm)
1. one thread merges two consecutive sorted sub arrays (If there are n cores in the system, then there are n sorted blocks. n/2 threads perform merge operations at this step. The other half of the cores int the system is not used on this step)
1. again, one thread merges two sorted blocks. (since there are n/2 sorted blocks at this iteration, n/4 threads take part in merging. Therefore 3/4 of cores remain idle.)
1. merge operation continues like this. In every iteration, the number of sorted blocks is reduced by half. In the last iteration, two sorted blocks are merged and a sorted element set is achieved. 

Parallel merge sort algorithm can be implemented both recursively and iteratively. Java library has two thread synchronization frameworks that help implementing parallel merge sort: 
* [CyclicBarrier](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CyclicBarrier.html): Synchrpnize a group of threads at barrier points possibly multiple times during a program. 
* [ForkJoin Framework](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinTask.html): One thread forks other threads and waits for their completion. Forked thrads can also fork new threads and recursively creates a thread tree. 

## Iterative Parallel Merge Sort
Iterative parallel merge sort algorithm works as explained above. A CyclicBarrier synchronizes all threads when moving to next iteration. First all threads sequentially sort their sub arrays, then they all wait for others to complete. When they completed sorting their sub arrays, they move to the next iteration. The CyclicBarrier makes sure that all threads wait each other between the iterations. 

## Recursive Parallel Merge Sort
Recursive parallel merge sort is implemented by using Fork Join framework in Java. A root thread divides the elements into two and forks two new threads to sort each of them. Then, recursively each thread divides its elements into two and forks two new threads to sort them. This thread construction continues until a threshold is hit. Usually when the number of threads in the leaf of the thread recursion three is equal to the number of cores in the system, recursion stops. Each thread in the leaf of recursion tree sorts its sub array. When they are done, non-leaf threads perform merging operation. In the last step, the root thread merges the last two sub sorted elements and produces a sorted element list. This algorithm is explained in below figure. 

![Recursive Parallel Merge Sort Algorithm](/docs/recursive-pms-3.png)

## Implementation of Iterative Parallel Merge Sort
I have implemented iterative parallel merge sort using a CyclicBarrier as explained above. I sort a long array to make it easy to understand. 
I have several implementation files. They are from simple to more complex. 
1. MergeSortWithBarriersSTM1.java: This file sorts a long array. It requires the number of threads to be a power of two. It also retquires that the number of elements to be sorted is divisible by the number of threads, so that each sub array is the same size. These two conditions make programming a little easier. This file performs single thread merging, not double thread merging. 
1. MergeSortWithBarriersSTM2.java: This is a more general version of MergeSortWithBarriersSTM1.java. It removes those two restrictions on the input. The number of threads can be an number and the number of elements can any length. This file also performs single thread merging, not double thread merging. 

## Implementation of Recursive Parallel Merge Sort
Similar to iterative implementations, I have two versions of recursive parallel merge sort algorithm. This one uses RecursiveAction class from ForkJoin framework. The algorithm is similar to the one shown above the figure. There is no double merging in these files either. 
1. MergeSortWithForkJoinSTM1.java: This file performs recursive merge sort on a long array. It requires the number of threads to be a power of two. It also retquires that the number of elements to be sorted is divisible by the number of threads. 
1. MergeSortWithForkJoinSTM2.java: This file performs recursive merge sort on a long array. It removes those two restrictions on the input. The number of threads can be an number and the number of elements can any length.

## Parallel Sort Implementation in Java Library
Java Arrays class in java.util package has a [parallelSort](https://docs.oracle.com/javase/8/docs/api/java/util/Arrays.html#parallelSort-long:A-) method to perform parallel sort on multi-core machines. It implements a recursive parallel merge sort by using ForkJoin framework. It is very similar to MergeSortWithForkJoinSTM2.java. 

