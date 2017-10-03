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

public class PerformanceTest {
	
	static int cores = Runtime.getRuntime().availableProcessors();
	static int iterations = 7;
	static int arraySizes[] = {10000000, 20000000, 30000000, 40000000, 50000000};
	static double avgDurationsSeq[] = new double[arraySizes.length];
	static double avgDurationsPar[] = new double[arraySizes.length];
	static double avgDurationsDM[] = new double[arraySizes.length];
	
	public static void main(String[] args) {
		
		for (int i = 0; i < arraySizes.length; i++) {
			runOneTestSet(arraySizes[i], i);
		}
		
		System.out.println("Array Sizes \tSeq Sort \tParallel Sort \tDouble Merge Sort");
		
		for (int i = 0; i < avgDurationsSeq.length; i++) {
			System.out.println(arraySizes[i]+"\t"+ avgDurationsSeq[i] + "\t\t" + avgDurationsPar[i]+"\t\t"+avgDurationsDM[i]);
		}
	}
	
	public static void runOneTestSet(int arraySize, int setNo) {
		long arraySeq[] = new long[arraySize];
		long arrayPar[] = new long[arraySize];
		long arrayDM[] = new long[arraySize];
		
		long durationsSeq[] = new long[iterations];
		long durationsPar[] = new long[iterations];
		long durationsDM[] = new long[iterations];
		
		long startTime, duration;
		
		for (int i = 0; i < iterations; i++) {
			// initialize arrays
			MergeSortUtil.arrayInit(arraySeq, arraySize+10+i);
			MergeSortUtil.arrayInit(arrayPar, arraySize+20+i);
			MergeSortUtil.arrayInit(arrayDM, arraySize+20+i);
			
			startTime = System.currentTimeMillis();
	        Arrays.sort(arraySeq); // system sequential sort
	        duration = System.currentTimeMillis() - startTime;
	        durationsSeq[i] = duration;
	        
			startTime = System.currentTimeMillis();
	        Arrays.parallelSort(arrayPar); // system parallel sort
	        duration = System.currentTimeMillis() - startTime;
	        durationsPar[i] = duration;
	        
			startTime = System.currentTimeMillis();
			MergeSortWithBarriersDTM2.parallelMergeSort(arrayDM, cores); // parallel sort with double merging
	        duration = System.currentTimeMillis() - startTime;
	        durationsDM[i] = duration;
		}
		
		long sumSeq=0, sumPar=0, sumDM=0;
		
		for (int i = 0; i < durationsDM.length; i++) {
			sumSeq += durationsSeq[i];
			sumPar += durationsPar[i];
			sumDM += durationsDM[i];
		}
		
		avgDurationsSeq[setNo] = sumSeq/iterations*1.0;
		avgDurationsPar[setNo] = sumPar/iterations*1.0;
		avgDurationsDM[setNo] = sumDM/iterations*1.0;
	}
}
