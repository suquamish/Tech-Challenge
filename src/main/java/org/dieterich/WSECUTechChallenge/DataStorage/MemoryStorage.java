package org.dieterich.WSECUTechChallenge.DataStorage;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Repository
public class MemoryStorage {
    private static Map<String, String> store = new ConcurrentHashMap<>();
    private static MemoryStorage instance = new MemoryStorage();

    public static MemoryStorage getInstance() {
        return instance;
    }

    private MemoryStorage() {
    }

    public synchronized void put(String key, String value, String groupId) {
        store.put(String.format("%s/%s", groupId, key), value);
    }

    private static MemoryStorageModel createModelFromEntry(Map.Entry<String, String> entry) {
        return new MemoryStorageModel().
                setGroupId(entry.getKey().split("/")[0]).
                setKey(entry.getKey().split("/")[1]).
                setValue(entry.getValue());
    }

    private boolean keyMatches(String key, Map.Entry<String, String> entry) {
        return entry.getKey().split("/")[1].equals(key);
    }

    private boolean keyValueMatches(Map.Entry<String, String> entry, String key, String value) {
        return entry.getValue().equals(value) && keyMatches(key, entry);
    }

    private boolean groupIdMatches(Map.Entry<String, String> entry, String groupId) {
        return entry.getKey().split("/")[0].equals(groupId);
    }

    public List<MemoryStorageModel> getByGroupId(String groupId) {
        return store.
                entrySet().
                stream().
                filter(entry -> groupIdMatches(entry, groupId)).
                map(entry -> createModelFromEntry(entry)).
                collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByKey(String key) {
        return store.
                entrySet().
                stream().
                filter(entry -> keyMatches(key, entry)).
                map(entry -> createModelFromEntry(entry)).
                collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByValue(String value) {
        return store.
                entrySet().
                stream().
                filter(entry -> entry.getValue().equals(value)).
                map(entry -> createModelFromEntry(entry)).
                collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByKeyValue(String key, String value) {
        return store.
                entrySet().
                stream().
                filter(entry -> keyValueMatches(entry, key, value)).
                map(MemoryStorage::createModelFromEntry
                ).collect(Collectors.toList());
    }

    public synchronized void deleteByGroupId(String groupId) {
        Object[] toRemove = store.
                keySet().
                stream().
                filter(entry -> entry.startsWith(groupId)).
                collect(Collectors.toSet()).
                toArray();

        for (Object key : toRemove) {
            store.remove(key.toString());
        }
    }
}