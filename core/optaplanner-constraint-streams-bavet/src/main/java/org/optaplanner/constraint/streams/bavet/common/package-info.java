/**
 * The contents of this package and its subpackages are performance-sensitive,
 * being directly on the hot path of the solver.
 * Various micro-optimizations have been applied as a result of extensive benchmarking.
 * Any changes to this code should be carefully tested against various OptaPlanner use cases and data set sizes.
 * Never make assumptions about this code, benchmark it instead.
 */
package org.optaplanner.constraint.streams.bavet.common;