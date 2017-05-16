**_This is the repository of the TinSpin benchmark framework. The TinSpin spatial index collection can be found [here](https://github.com/tzaeschke/tinspin-indexes)._**

# TinSpin

TinSpin is a framework for benchmarking in-memory spatial indexes. 

TinSpin provides several dataset generators for point data and rectangle data. The datasets can be scaled with size and dimensionality. Each index can be tested with various loads, such as insertion, window queries, exact match queries, nearest neighbor queries, updates (moving objects) and removal. 




# Output File Format

The test data is written to tab-separated value files in target/logs.

__** Folders **__

There are several possible subfolders, which are be defined in the `TestManager` class. The default folders are:

 * dimsP: Point data scaled with dimensionality
 * dimsR: Rectangle data scaled with dimensionality
 * sizeP: Point data scaled with dataset size
 * sizeR: Rectangle data scaled with dataset size
 * sizePWQS: Point data scaled with size of query window
 * sizeRWQS: Rectangle data scaled with size of query window 

__** File Sections **__

 * column names: `Index data ...`
 * comment: `% Averages`
 * comment: `% ========`
 * averaged results: `RSZ-R-AVG-3/3 ...`  (Index=RSZ, datatype=Rectangle, average of 3 successful runs of 3 initiated runs
 * comment: `% Measurements`
 * comment: `% ============`
 * results: `RSZ-R-0	CLUSTER(5.0,0.0,null) ...` (Index=RSZ, datatype=Rectangle, random seed=0 (equals test run ID)
 
By default, TinSpin averages three consequtive test runs into one average. 

__** File Columns **__


 * Index: Index and test descriptor, such as `RSZ-R` for rectangle index, see above
 * data: Test data descriptor, Such as `CUBE(1.0,0.0,null)`, see above
 * dim: number of data dimensions
 * bits: number of bits (deprecated), always 64
 * N: dataset size (number of points or rectangles)
 * calcMem: Estimated memory requirement [bytes] per entry, only PH-Tree variants
 * memory: Total measured JVM memory [bytes]
 * memory/n: Total measured JVM memory [bytes per entry]
 
Timing. Most parts of the test are executed in two runs, each run consisting of a predefined number of execution. For example, each exact match run consists of 100,000 exact match queries as defined in the `TestStats` class.
 
 * gen: time [ms] for dataset generation
 * load: total index loading time [ms]
 * load/n: average loading time [micro-s/entry]
 * q1/n: window query time run #1 [micro-s/returned entry]
 * q2/n: window query time run #2 [micro-s/returned entry]
 * pq1/n: exact match query time run #1 [micro-s/query] (was called point query)
 * pq2/n: exact match query time run #2 [micro-s/query]
 * up1/n: update time run #1 [micro-s/update]
 * up2/n: update time run #2 [micro-s/update]
 * 1-NN1: query time run #1 [micro-s/update]
 * 1-NN2: query time run #2 [micro-s/update] 
 * 10-NN1: query time run #1 [micro-s/update] 
 * 10-NN2: query time run #2 [micro-s/update] 
 * unload: total index unloading time [ms]
 * unload/n: average removal time [micro-s/entry]
 
Tree statistics. The following columns contain tree statistics, such as number of nodes or depth. The meaning may differ between trees. 
 
 * nodes: Number of nodes
 * postLen: PH:Average length of postfixes; R-Tree&Quadtrees: depth
 * AHC: PH:Number of AHC nodes
 * NT: PH:Number of NT-Nodes
 * NTinternal: PH:Number of NT-subnodes in all NT-Nodes
 
Result statistics. The following columns give an indicator of the result returned by the test runs. For averaged results they contain the averages. 
 
 * q1-n: Number of returned objects
 * q2-n: Number of returned objects
 * q1p-n: Number of found objects
 * q2p-n: Number of found objects
 * up1-n: Number of found objects
 * up2-n: Number of found objects
 * d1-1NN: Average distance of nearest neighbors
 * d2-1NN: Average distance of nearest neighbors
 * d1-kNN: Average of sum of distance of 10 nearest neighbors
 * d2-kNN: Average of sum of distance of 10 nearest neighbors
 
For each test part, the following column contain garbage collection statistics based on Java instrumentation. They are a good indicator, but not precise! `-s` is the estimated memory [MB] freed up by GC. `-t` is the estimated time in [ms] used by the GC.  
 
 * load-s: Estimated size of garbage collected memory [MB] 
 * load-t: Estimated runtime of garbage collector [ms] 
 * w-query-s
 * w-query-t
 * p-query-s
 * p-query-t
 * update-s
 * update-t
 * 1-NN-s
 * 1-NN-t
 * 10-NN-s
 * 10-NN-t
 * unload-s
 * unload-t

General messages column: 

 * msg: General messages by index wrapper and test runner
 
 


