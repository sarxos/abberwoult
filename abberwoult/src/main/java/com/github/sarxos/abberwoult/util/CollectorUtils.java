package com.github.sarxos.abberwoult.util;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Some {@link Collector}-related utilities.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class CollectorUtils {

	private static final Deque<Object> EMPTY_DEQUE = new EmptyDeque<>();

	/**
	 * Collect {@link Stream} elements into the {@link ArrayList} with the same size as the input
	 * collection. Example use case:<br>
	 *
	 * <pre>
	 * // there is 5 elements on input, thus collect to list of size 5
	 * List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
	 * List<Integer> list = input.stream()
	 * 	.map(i -> i * i)
	 * 	.collect(CollectorUtils.toListWithSameSizeAs(input));
	 * </pre>
	 *
	 * @param collection the {@link Collection} to take size from
	 * @return New {@link Collector}
	 */
	public static <T> Collector<T, ?, List<T>> toListWithSameSizeAs(final Collection<?> collection) {
		final int size = collection == null ? 0 : collection.size();
		return toListWithSizeOf(size);
	}

	public static <T> Collector<T, ?, List<T>> toListWithSameSizeAs(final Object[] array) {
		final int size = array == null ? 0 : array.length;
		return toListWithSizeOf(size);
	}

	public static <T> Collector<T, ?, Set<T>> toSetWithSameSizeAs(final Collection<?> collection) {
		final int size = collection == null ? 0 : collection.size();
		return toSetWithSizeOf(size);
	}

	/**
	 * Collect {@link Stream} elements into the {@link ArrayList} with a given size. Example use
	 * case:<br>
	 *
	 * <pre>
	 * // there is 5 elements on input, thus collect to list of size 5
	 * List<Integer> list = Arrays.asList(1, 2, 3, 4, 5).stream()
	 * 	.map(i -> i * i)
	 * 	.collect(CollectorUtils.toListWithSizeOf(5));
	 * </pre>
	 *
	 * @param size the {@link ArrayList} size
	 * @return New {@link Collector}
	 */
	public static <T> Collector<T, ?, List<T>> toListWithSizeOf(final int size) {
		return Collectors.toCollection(() -> new ArrayList<>(size));
	}

	/**
	 * Collect {@link Stream} elements into the {@link HashSet} with a given size and load factor of
	 * 1.0. Example use case:<br>
	 *
	 * <pre>
	 * // there is 5 elements on input, thus collect to set of size 5
	 * List<Integer> list = Arrays.asList(1, 2, 3, 4, 5).stream()
	 * 	.map(i -> i * i)
	 * 	.collect(CollectorUtils.toSetWithSizeOf(5));
	 * </pre>
	 *
	 * @param size the {@link HashSet} size
	 * @return New {@link Collector}
	 */
	public static <T> Collector<T, ?, Set<T>> toSetWithSizeOf(final int size) {
		return Collectors.toCollection(() -> new HashSet<>(size, 1.0f));
	}

	public static <T> Set<T> toSet(final Iterable<T> iterable) {
		return StreamSupport
			.stream(iterable.spliterator(), false)
			.collect(Collectors.toCollection(HashSet::new));
	}

	public static <T> Set<T> toLinkedSet(final Iterable<T> iterable) {
		return StreamSupport
			.stream(iterable.spliterator(), false)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Return {@link Stream} of input {@link Collection} elements or empty stream in case when input
	 * collection is null.
	 *
	 * @param collection the collection to stream elements from
	 * @return {@link Stream} of elements
	 */
	public static <T> Stream<T> stream(final Collection<T> collection) {
		if (collection == null) {
			return Stream.empty();
		} else {
			return collection.stream();
		}
	}

	public static <T> Stream<T> stream(final T[] array) {
		if (array == null) {
			return Stream.empty();
		} else {
			return Arrays.stream(array);
		}
	}

	public static <T> Stream<T> stream(final Spliterator<T> spliterator) {
		return StreamSupport.stream(spliterator, false);
	}

	public static <K, T> Map<K, T> map(final Collection<T> collection, Function<T, K> keyMapper) {
		final int size = collection == null ? 0 : collection.size();
		final Map<K, T> map = new HashMap<>(size, 1f);
		return stream(collection).collect(toMap(keyMapper, identity(), (a, b) -> a, () -> map));
	}

	public static <K, T, U> Map<K, U> map(final Collection<T> collection, Function<T, K> keyMapper, Function<T, U> valueMapper) {
		final int size = collection == null ? 0 : collection.size();
		final Map<K, U> map = new HashMap<>(size, 1f);
		return map(collection, () -> map, keyMapper, valueMapper);
	}

	public static <K, T, U> Map<K, U> map(final Collection<T> collection, final Supplier<Map<K, U>> mapSupplier, Function<T, K> keyMapper, Function<T, U> valueMapper) {
		return stream(collection).collect(toMap(keyMapper, valueMapper, (a, b) -> a, mapSupplier));
	}

	public static <T> List<T> optimizedList(final Collection<T> list) {
		return list.stream().collect(toListWithSameSizeAs(list));
	}

	@SuppressWarnings("unchecked")
	public static <T> Deque<T> emptyDeque() {
		return (Deque<T>) EMPTY_DEQUE;
	}
}

final class EmptyDeque<T> implements Deque<T> {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Object[] toArray() {
		return EMPTY_ARRAY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> R[] toArray(R[] a) {
		return (R[]) EMPTY_ARRAY;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Adding elements to this deque is forbidden");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
		// do nothing
	}

	@Override
	public void addFirst(T e) {
		throw new UnsupportedOperationException("Adding element to this deque is forbidden");
	}

	@Override
	public void addLast(T e) {
		throw new UnsupportedOperationException("Adding element to this deque is forbidden");
	}

	@Override
	public boolean offerFirst(T e) {
		throw new UnsupportedOperationException("Adding element to this deque is forbidden");
	}

	@Override
	public boolean offerLast(T e) {
		throw new UnsupportedOperationException("Adding element to this deque is forbidden");
	}

	@Override
	public T removeFirst() {
		throw new NoSuchElementException("This deque is empty by default");
	}

	@Override
	public T removeLast() {
		throw new NoSuchElementException("This deque is empty by default");
	}

	@Override
	public T pollFirst() {
		return null;
	}

	@Override
	public T pollLast() {
		return null;
	}

	@Override
	public T getFirst() {
		throw new NoSuchElementException("This deque is empty by default");
	}

	@Override
	public T getLast() {
		throw new NoSuchElementException("This deque is empty by default");
	}

	@Override
	public T peekFirst() {
		return null;
	}

	@Override
	public T peekLast() {
		return null;
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		return false;
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		return false;
	}

	@Override
	public boolean add(T e) {
		return false;
	}

	@Override
	public boolean offer(T e) {
		return offerLast(e);
	}

	@Override
	public T remove() {
		return removeFirst();
	}

	@Override
	public T poll() {
		return pollFirst();
	}

	@Override
	public T element() {
		return getFirst();
	}

	@Override
	public T peek() {
		return peekFirst();
	}

	@Override
	public void push(T e) {
		throw new UnsupportedOperationException("Adding element to this deque is forbidden");
	}

	@Override
	public T pop() {
		return removeFirst();
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<T> descendingIterator() {
		return Collections.emptyIterator();
	}
}
