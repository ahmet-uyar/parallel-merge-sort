# Parallel Merge Sort With Double Merging

Sorting is one of the most important problems in computer science. There are many sequential sorting algorithms. However, many of those sequential sorting algorithms are not suitable for parallelization. 

In this project, I implemented two types of parallel merge sort algorithm: iterative and recursive. I compare my algorithm with the parallel merge sort algorithm in Java library. My implementations have two advantages compared to the parallel merge sort in Java library: 
* My iterative parallel merge sort algorithm performs simultanenous double merging. It utilizes multi-core CPUs better and yields greater performance. Performance comparison figures are given below. 
* My recursive parallel merge sort algorithm divides the work among the cores better by constructing better recursive binary thread trees.  

## Parallel Merge Sort Algorithm
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

## Parallel Merge Sort With Double Merging
The main insight of this algorithm is that each merging operation can be performed by two threads simultanenously. One thread can get minimums of two sorted sub arrays repeatedly. Until it merges half of the elements. The other thread can get maximums of two sorted sub arrays repeatedly. Until it merges the other half of the elements. Double merging is explained in the below figure. The details are explained in the [paper](http://ieeexplore.ieee.org/document/7036012/). 

![Double Merging Algorithm](/docs/double-merge-2.png)

### Synchronization of Double Merging Threads
When two threads are performing simultanenous merging, they need to wait each other at two points. The algorithm is shown below: 
```
Merging starts{
   Merge: 
      Thread 1: merge mins
      Thread 2: merge maxes
   Synchronize: 
      Two threads wait each other
   Copy back:
      Thread 1: copy back first half
      Thread 2: copy back second half
   Synchronize: 
      Two threads wait each other
} Merging ends
```

### Utilization of Cores
Double merging algorithm improves the merging speed two times. With the previous algorithm, at the first iterations of merge operations, only half of the cores are used to perform merging. The other half of the cores sit idle. With double merging algorithm, all cores in the system is used to perform merging in first iteration of merging. Similarly in other iterations, double merging utilizes two times more cores in the system. 

### Implementation of Iterative Merge Sort With Double Merging
I have implemented the double merge algorithm using iterative parallel merge sort. It has two versions. 
1. MergeSortWithBarriersDTM1.java: It performs iterative merge sort on a long array. It requires the number of threads to be a power of two. It also requires that the number of elements to be sorted is divisible by the number of threads. 
1. MergeSortWithBarriersDTM2.java: This file performs iterative merge sort on a long array. It removes those two restrictions on the input. The number of threads can be an number and the number of elements can any length.

## Performance Comparions
I compared the performance of double thread merging algorithm with the single theard merging algorithm in Java library. I also compared them with sequential sorting times from Arrays.sort method in Java library. Performance comparison class is PerformanceTest.java. 
* I tested the sorting times with 5 different array sizes: 10M, 20M, 30M, 40M, 50M. 
* I run each sorting 7 times and average the running times. This helps soothing out the interferences from other running processes on the computer. 
* Below figure shows the results. Compared to the Java Library parallel sort method, double merging algorithm provides 15-20% performance gains on a quadcore machine. 

![Performance Comparison](/docs/perf-compare.png)

## Conclusion
* In summary, parallel merge sort with double merging algorithm provides a nice performance gain compared to the parallel merge sort algorithm implemented in Java Library. 
* I hope these programs may be helpful for those people who would like to learn parallel programming with barriers or fork-join framework. Parallel merge sort is a good example problem to study when learnin parallel programming. 

## Further Study
* It would be interesting to see the performance gains in systems with higher number of cores. 
* Implementation of double merging with fork-join can be done. In that case, two children threads need to synchronize with each other when performing simultaneous merging. That is kind of problematic with ForkJoin framework however. 
