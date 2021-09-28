package com.bennyhuo.java17;

import java.io.ObjectInputFilter;
import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Created by benny.
 */
public class SerializationFilter {

    public static void main(String[] args) {

    }

    static class Person implements Serializable {
        int id;
        String name;
    }

    static class MyFilter implements ObjectInputFilter {

        @Override
        public Status checkInput(FilterInfo filterInfo) {
            return null;
        }
    }

    static class MyFilterFactory implements BinaryOperator<ObjectInputFilter> {

        @Override
        public ObjectInputFilter apply(ObjectInputFilter objectInputFilter, ObjectInputFilter objectInputFilter2) {
            return null;
        }

    }

    public static class FilterInThread implements BinaryOperator<ObjectInputFilter> {

        // ThreadLocal to hold the serial filter to be applied
        private final ThreadLocal<ObjectInputFilter> filterThreadLocal = new ThreadLocal<>();

        // Construct a FilterInThread deserialization filter factory.
        public FilterInThread() {}

        /**
         * The filter factory, which is invoked every time a new ObjectInputStream
         * is created.  If a per-stream filter is already set then it returns a
         * filter that combines the results of invoking each filter.
         *
         * @param curr the current filter on the stream
         * @param next a per stream filter
         * @return the selected filter
         */
        public ObjectInputFilter apply(ObjectInputFilter curr, ObjectInputFilter next) {
            if (curr == null) {
                // Called from the OIS constructor or perhaps OIS.setObjectInputFilter with no current filter
                var filter = filterThreadLocal.get();
                if (filter != null) {
                    // Prepend a filter to assert that all classes have been Allowed or Rejected
                    filter = ObjectInputFilter.rejectUndecidedClass(filter);
                }
                if (next != null) {
                    // Prepend the next filter to the thread filter, if any
                    // Initially this is the static JVM-wide filter passed from the OIS constructor
                    // Append the filter to reject all UNDECIDED results
                    filter = ObjectInputFilter.merge(next, filter);
                    filter = ObjectInputFilter.rejectUndecidedClass(filter);
                }
                return filter;
            } else {
                // Called from OIS.setObjectInputFilter with a current filter and a stream-specific filter.
                // The curr filter already incorporates the thread filter and static JVM-wide filter
                // and rejection of undecided classes
                // If there is a stream-specific filter prepend it and a filter to recheck for undecided
                if (next != null) {
                    next = ObjectInputFilter.merge(next, curr);
                    next = ObjectInputFilter.rejectUndecidedClass(next);
                    return next;
                }
                return curr;
            }
        }

        /**
         * Apply the filter and invoke the runnable.
         *
         * @param filter the serial filter to apply to every deserialization in the thread
         * @param runnable a Runnable to invoke
         */
        public void doWithSerialFilter(ObjectInputFilter filter, Runnable runnable) {
            var prevFilter = filterThreadLocal.get();
            try {
                filterThreadLocal.set(filter);
                runnable.run();
            } finally {
                filterThreadLocal.set(prevFilter);
            }
        }
    }

}
