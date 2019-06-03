package org.dieterich.WSECUTechChallenge.DataStorage;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;


@Repository
public class MemoryStorage {
    private static Map<String, String> store = new HashMap<>();
    private static MemoryStorage instance = new MemoryStorage();

    public static MemoryStorage getInstance() {
        return instance;
    }

    private MemoryStorage() {}

    public synchronized void put(String key, String value, String groupId) {
        store.put(String.format("%s/%s", groupId, key), value);
    }

    public List<MemoryStorageModel> getByGroupId(String groupId) {
        return store.
                entrySet().
                stream().
                filter(entry ->
                        entry.getKey().split("/")[0].equals(groupId)
                ).
                map(entry ->
                    new MemoryStorageModel().
                                    setGroupId(entry.getKey().split("/")[0]).
                                    setKey(entry.getKey().split("/")[1]).
                                    setValue(entry.getValue())
                        ).collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByKey(String key) {
        return store.
                entrySet().
                stream().
                filter(entry ->
                        entry.getKey().split("/")[1].equals(key)
                ).
                map(entry ->
                        new MemoryStorageModel().
                                setGroupId(entry.getKey().split("/")[0]).
                                setKey(entry.getKey().split("/")[1]).
                                setValue(entry.getValue())
                ).collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByValue(String value) {
        return store.
                entrySet().
                stream().
                filter(entry ->
                        entry.getValue().equals(value)
                ).
                map(entry ->
                        new MemoryStorageModel().
                                setGroupId(entry.getKey().split("/")[0]).
                                setKey(entry.getKey().split("/")[1]).
                                setValue(entry.getValue())
                ).collect(Collectors.toList());
    }

    public List<MemoryStorageModel> getByKeyValue(String key, String value) {
        return store.
                entrySet().
                stream().
                filter(entry ->
                        entry.getValue().equals(value) && entry.getKey().split("/")[1].equals((key))
                ).
                map(entry ->
                        new MemoryStorageModel().
                                setGroupId(entry.getKey().split("/")[0]).
                                setKey(entry.getKey().split("/")[1]).
                                setValue(entry.getValue())
                ).collect(Collectors.toList());
    }
}