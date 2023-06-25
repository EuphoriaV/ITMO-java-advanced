package info.kgeorgiy.ja.kuznetsov.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List which supports parallel changes.
 *
 * @author Kuznetsov Ilya (ilyakuznecov84@gmail.ru)
 */
public class ParallelList<R> {
    private int left;
    private final List<R> result;

    /**
     * Constructor from count of list values.
     */
    public ParallelList(int count) {
        this.left = count;
        this.result = new ArrayList<>(Collections.nCopies(count, null));
    }

    /**
     * Set value in certain list position and decrease number of unset values.
     *
     * @param index position in list where value will be set in.
     * @param value value that will be set.
     */
    public synchronized void set(int index, R value) {
        result.set(index, value);
        left--;
        if (left == 0) {
            notify();
        }
    }

    /**
     * Returns list after setting all values.
     *
     * @return {@link List} list with set values.
     */
    public synchronized List<R> getResult() {
        while (left != 0) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        return result;
    }
}