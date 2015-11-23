package org.littlewings.infinispan.continuousquery;

import java.util.LinkedHashMap;
import java.util.Map;

import org.infinispan.query.continuous.ContinuousQueryResultListener;

public class MyContinuousQueryListener<K, V> implements ContinuousQueryResultListener<K, V> {
    private Map<K, V> joined = new LinkedHashMap<>();
    private Map<K, Integer> joinCalled = new LinkedHashMap<>();
    private Map<K, Integer> leaveCalled = new LinkedHashMap<>();

    @Override
    public void resultJoining(K key, V value) {
        synchronized (joined) {
            joined.put(key, value);
            incrementNumberOfCalls(joinCalled, key);
        }
    }

    @Override
    public void resultLeaving(K key) {
        incrementNumberOfCalls(leaveCalled, key);
    }

    void incrementNumberOfCalls(Map<K, Integer> countingMap, K key) {
        synchronized (countingMap) {
            countingMap.compute(key, (k, v) -> v == null ? 1 : v + 1);
        }
    }

    public Map<K, V> getJoined() {
        return joined;
    }

    public Map<K, Integer> getJoinCalled() {
        return joinCalled;
    }

    public Map<K, Integer> getLeaveCalled() {
        return leaveCalled;
    }
}
