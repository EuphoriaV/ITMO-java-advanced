package info.kgeorgiy.ja.kuznetsov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.function.Function;

/**
 * Class that apply function on list of values in parallel.
 *
 * @author Kuznetsov Ilya (ilyakuznecov84@gmail.ru)
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final ParallelQueue queue;
    private final Thread[] threads;

    /**
     * Constructor from number of threads
     *
     * @param threadsCount number of threads
     */
    public ParallelMapperImpl(int threadsCount) {
        queue = new ParallelQueue();
        threads = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(() -> {
                while (true) {
                    try {
                        queue.poll().run();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performed in parallel.
     *
     * @param f    function that will be applied on list values.
     * @param args list with values which function will be applied on.
     * @return {@link List} list with values after applying the function.
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) {
        ParallelList<R> res = new ParallelList<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            int finalI = i;
            queue.add(() -> res.set(finalI, f.apply(args.get(finalI))));
        }
        return res.getResult();
    }

    /**
     * Stops all threads. All unfinished mappings are left in undefined state.
     */
    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}