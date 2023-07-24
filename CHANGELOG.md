# Changelog

## 2023-07-24

- (TZ) Version bumbed to 1.1.0
- (TZ) tinspin-indexes 2.0.0
- (TZ) Changed data type of indexes from Singleton Object (or sometimes double[]) to Integer ID.

## 2023-07-04

- (TZ) Added support for multimap tests
- (TZ) Updated README with proper description of output format 

## 2018-12-10

- (TZ) Modularization, new base module tinspin-common
- (TZ) Added new Example

## 2018-11-23

- (TZ) Added (very basic!) import for HDF5 files.
- (TZ) Updated Window Query generator again
- (TZ) Re-added repeated testing of update()
- (TZ) Use result count only from first run, for comparability
- (TZ) Updated timings to use nanoSecs internally
- (TZ) CHANGE: WQ timings now return time/query instead of time/result

## 2018-07-10

- (TZ) New logging output with ops/sec instead of time/op
- (TZ) Operations without side effects (window query, point query, kNN search) are repeatedly
  executed until a minimum time has passed (in order to avoid problems with warm-up)

## 2018-05-18

- (TZ) Fixed window query generator for dim>40

## 2017-12-04

- (TZ) Updated reference to tinspin-indexes to 1.5.1

## 2017-11-13

- (TZ) Updated reference to tinspin-indexes to 1.4.0
- (TZ) Added Wrapper for TinSpin-Indexes KD-Tree

## 2017-01-03

- (TZ) Added CLUSTER Gauss generators, cleaned up update distance, general cleanup

## 2016-09-18

- (TZ) Refactored random number generation to be reinitialized for each task.
