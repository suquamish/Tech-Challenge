package org.dieterich.TechChallenge.Models;

public class MemoryStorageModel {
    private String groupId;
    private String key;
    private String value;

    public MemoryStorageModel setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public MemoryStorageModel setKey(String key) {
        this.key = key;
        return this;
    }

    public String getKey() {
        return key;
    }

    public MemoryStorageModel setValue(String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }
}
