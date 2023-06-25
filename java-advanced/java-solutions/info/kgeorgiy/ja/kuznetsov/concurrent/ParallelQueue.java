package info.kgeorgiy.ja.kuznetsov.concurrent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Queue which supports parallel changes.
 *
 * @author Kuznetsov Ilya (ilyakuznecov84@gmail.ru)
 */
public class ParallelQueue {
    private final Queue<Runnable> tasks;

    /**
     * Default constructor.
     */
    public ParallelQueue() {
        this.tasks = new LinkedList<>();
    }

    /**
     * Polls object from the beginning of the queue. If it is empty queue will wait until
     * new object will be added.
     *
     * @return {@link Runnable} polled object.
     */
    public synchronized Runnable poll() throws InterruptedException {
        while (tasks.isEmpty()) {
            wait();
        }
        return tasks.poll();
    }

    /**
     * Pushes object in the end of the queue and notifies that it is not empty anymore.
     *
     * @param task object that will be added.
     */
    public synchronized void add(Runnable task) {
        tasks.add(task);
        notify();
    }
}