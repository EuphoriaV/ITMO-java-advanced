package info.kgeorgiy.ja.kuznetsov.arrayset;

import java.util.*;

// :NOTE: AbstractSet or AbstractCollection
public class ArraySet<T> extends AbstractCollection<T> implements SortedSet<T> {

    private final Comparator<T> comparator;
    private final List<T> list;

    public ArraySet() {
        this(List.of(), null);
    }

    public ArraySet(Collection<T> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {
        this(removeDuplicates(collection, comparator), comparator);
    }

    private ArraySet(List<T> list, Comparator<T> comparator) {
        this.list = list;
        this.comparator = comparator;
    }

    private static <T> List<T> removeDuplicates(Collection<? extends T> collection, Comparator<? super T> comparator) {
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        return List.copyOf(treeSet);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    private int find(T x) {
        return Collections.binarySearch(list, x, comparator);
    }

    @SuppressWarnings("unchecked")
    private int compare(T a, T b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        } else {
            Comparable<? super T> x = (Comparable<? super T>) a;
            return x.compareTo(b);
        }
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        int l = findMoreOrEqualThan(fromElement);
        int r = findLessThan(toElement);
        return new ArraySet<>(list.subList(l, r + 1), comparator);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        int l = findLessThan(toElement);
        return new ArraySet<>(list.subList(0, l + 1), comparator);
    }

    private int findLessThan(T toElement) {
        int ans = find(toElement);
        return ans >= 0 ? ans - 1 : -ans - 2;
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        int l = findMoreOrEqualThan(fromElement);
        return new ArraySet<>(list.subList(l, size()), comparator);
    }

    private int findMoreOrEqualThan(T fromElement) {
        int ans = find(fromElement);
        return ans >= 0 ? ans : -ans - 1;
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(size() - 1);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return find((T) o) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);
    }
}
