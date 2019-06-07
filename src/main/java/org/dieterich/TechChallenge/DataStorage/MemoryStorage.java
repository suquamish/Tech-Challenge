package org.dieterich.TechChallenge.DataStorage;

import org.dieterich.TechChallenge.Exceptions.NoCollectionFoundException;
import org.dieterich.TechChallenge.Models.MemoryStorageModel;
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

    private MemoryStorage() {}

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

    private String generateGroupId() { return UUID.randomUUID().toString(); }

    public synchronized List<MemoryStorageModel> put(String key, String value, String groupId) throws NoCollectionFoundException {
        MemoryStorageModel item = new MemoryStorageModel();
        item.setGroupId(groupId);
        item.setKey(key);
        item.setValue(value);

        if (groupId == null || groupId.isEmpty()) {
            item.setGroupId(generateGroupId());
        } else {
            store.
                    keySet().
                    parallelStream().
                    filter(k -> k.startsWith(groupId)).
                    findAny().
                    orElseThrow(NoCollectionFoundException::new);
        }
        store.put(String.format("%s/%s", item.getGroupId(), item.getKey()), item.getValue());
        List<MemoryStorageModel> result = new ArrayList<>();
        result.add(item);
        return result;
    }

    public synchronized List<MemoryStorageModel> put(String key, String value) {
        return put(key, value, null);
    }

    public List<MemoryStorageModel> getByGroupId(String groupId) {
        return store.
                entrySet().
                parallelStream().
                filter(entry -> groupIdMatches(entry, groupId)).
                map(entry -> createModelFromEntry(entry)).
                collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByKey(String key) {
        return store.
                entrySet().
                parallelStream().
                filter(entry -> keyMatches(key, entry)).
                map(entry -> createModelFromEntry(entry)).
                collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByValue(String value) {
        return store.
                entrySet().
                parallelStream().
                filter(entry -> entry.getValue().equals(value)).
                map(entry -> createModelFromEntry(entry)).
                collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByKeyValue(String key, String value) {
        return store.
                entrySet().
                parallelStream().
                filter(entry -> keyValueMatches(entry, key, value)).
                map(MemoryStorage::createModelFromEntry
                ).collect(Collectors.toList());
    }

    public synchronized void deleteByGroupId(String groupId) {
        if (groupId == null || groupId.isEmpty()) return;

        Object[] toRemove = store.
                keySet().
                parallelStream().
                filter(entry -> entry.startsWith(groupId)).
                collect(Collectors.toSet()).
                toArray(); // .toArray() here to avoid the false ConcurrentAccessException

        for (Object key : toRemove) {
            store.remove(key.toString());
        }
    }
}