package betterquesting.api.storage;

import java.util.List;

public interface IRegStorageBase<K, V> {
	K nextKey();
	void add(V value, K key);
	boolean removeKey(K key);
	boolean removeValue(V value);
	V getValue(K key);
	K getKey(V value);
	int size();
	void reset();
	List<V> getAllValues();
	List<K> getAllKeys();
}