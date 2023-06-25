package info.kgeorgiy.ja.kuznetsov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Scalar iterative parallelism support.
 *
 * @author Kuznetsov Ilya (ilyakuznecov84@gmail.ru)
 */
public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper parallelMapper;

    /**
     * Default constructor.
     */
    public IterativeParallelism() {
        parallelMapper = null;
    }

    /**
     * Constructor which initialize parallelMapper with given one.
     *
     * @param parallelMapper given parallelMapper
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return maximum of given values
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) {
        if (values == null || values.size() == 0) {
            throw new NoSuchElementException("List of values must be not empty");
        }
        return getResult(threads, values, list -> Collections.max(list, comparator),
                list -> Collections.max(list, comparator));
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return minimum of given values
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) {
        if (values == null || values.size() == 0) {
            throw new NoSuchElementException("List of values must be not empty");
        }
        return getResult(threads, values, list -> Collections.min(list, comparator),
                list -> Collections.min(list, comparator));
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether all values satisfy predicate or {@code true}, if no values are given.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) {
        if (values == null || values.size() == 0) {
            return true;
        }
        return getResult(threads, values, list -> list.stream().allMatch(predicate),
                list -> list.stream().reduce(true, Boolean::logicalAnd));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) {
        if (values == null || values.size() == 0) {
            return true;
        }
        return getResult(threads, values, list -> list.stream().anyMatch(predicate),
                list -> list.stream().reduce(false, Boolean::logicalOr));
    }

    /**
     * Returns number of values satisfying predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return number of values satisfying predicate.
     */
    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) {
        if (values == null || values.size() == 0) {
            return 0;
        }
        return getResult(threads, values, list -> (int) list.stream().filter(predicate).count(),
                list -> list.stream().reduce(0, Integer::sum));
    }

    private <T, R> R getResult(int threadsCount, List<? extends T> values, Function<List<? extends T>, R> function,
                               Function<List<R>, R> resultFunction) {
        threadsCount = Math.min(threadsCount, values.size());
        int mod = values.size() % threadsCount;
        int div = values.size() / threadsCount;
        int count = 0;
        int cur = 0;
        List<List<? extends T>> parts = new ArrayList<>();
        for (int i = 0; i < threadsCount; i++) {
            int l = cur;
            cur += div + (mod > count ? 1 : 0);
            count++;
            int r = cur;
            parts.add(values.subList(l, r));
        }
        if (parallelMapper == null) {
            List<R> result = new ArrayList<>(Collections.nCopies(threadsCount, null));
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < threadsCount; i++) {
                int finalI = i;
                threads.add(new Thread(() -> result.set(finalI, function.apply(parts.get(finalI)))));
            }
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException ignored) {
                }
            }
            return resultFunction.apply(result);
        } else {
            List<R> result = null;
            try {
                result = parallelMapper.map(function, parts);
            } catch (InterruptedException ignored) {
            }
            return resultFunction.apply(result);
        }
    }
}
