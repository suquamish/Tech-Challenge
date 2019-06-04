package org.dieterich.WSECUTechChallenge

import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorageModel
import spock.lang.Specification

class MemoryStorageUnitTest extends Specification {
    MemoryStorage subject

    def setup() {
        subject = MemoryStorage.getInstance()
        subject.metaClass.static.setStore = { newObj ->
            store = newObj
        }
        subject.metaClass.static.getStore = { store }
    }

    def cleanup() {
        subject.setStore(new HashMap<String, String>())
    }

    def "should be a singleton"() {
        expect:
        subject == MemoryStorage.getInstance()
    }

    def "allows me to store data"() {
        given:
        def groupId = UUID.randomUUID().toString()
        def key = "somekey"
        def value = "somevalue"
        HashMap mockHashMap = Mock(HashMap)
        subject.setStore(mockHashMap)
        assert mockHashMap == subject.getStore()


        when:
        subject.put(key, value, groupId)

        then:
        1 * mockHashMap.put("${groupId}/${key}", value)
    }

    def "allows me to retrieve data by groupid"() {
        given:
        def groupId = UUID.randomUUID().toString()
        subject.put("a key", "a value", groupId)

        when:
        def result = subject.getByGroupId(groupId)

        then:
        assert result instanceof List
        assert result.first().groupId == groupId
        assert result.first().key == "a key"
        assert result.first().value == "a value"
    }

    def "allows me to retrieve all data with matching groupids"() {
        given:
        def groupId = UUID.randomUUID().toString()
        subject.put("first key", "first value", groupId)
        subject.put("second key", "second value", groupId)
        subject.put("third key", "third value", UUID.randomUUID().toString())
        List expectedResults = [
                new MemoryStorageModel(key: "first key", value: "first value", groupId: groupId),
                new MemoryStorageModel(key: "second key", value: "second value", groupId: groupId),
        ]

        when:
        def results = subject.getByGroupId(groupId)

        then:
        assert results instanceof List
        assert results.size() == expectedResults.size()
        expectedResults.each { r ->
            assert results.find { e -> e.groupId == r.groupId && e.key == r.key && e.value == r.value }
        }
    }

    def "allows me to retrieve all data with matching keys"() {
        given:
        subject.put("name", "Joey McJoe", UUID.randomUUID().toString())
        subject.put("name", "Andy Vanandy", UUID.randomUUID().toString())
        subject.put("email", "frothy@example.com", UUID.randomUUID().toString())

        when:
        def results = subject.getByKey("name")

        then:
        assert results instanceof List
        assert results.size() == 2
        ["Joey McJoe", "Andy Vanandy"].each { r ->
            assert results.find { e -> e.key == "name" && e.value == r }
        }
        assert !results.find { e -> e.key ==  "email" }
    }

    def "allows me to retrieve all data with matching values"() {
        given:
        subject.put("name", "Joey McJoe", UUID.randomUUID().toString())
        subject.put("email", "frothy@example.com", UUID.randomUUID().toString())
        subject.put("username", "frothy@example.com", UUID.randomUUID().toString())

        when:
        def results = subject.getByValue("frothy@example.com")

        then:
        assert results instanceof List
        assert results.size() == 2
        results.each { e -> assert (e.key == "email" || e.key == "username") && e.value == "frothy@example.com" }
        assert !results.find { e -> e.key ==  "name" }
    }

    def "allows me to retrieve all data with matching key-value pairs"() {
        given:
        subject.put("name", "Joey McJoe", UUID.randomUUID().toString())
        subject.put("email", "frothy@example.com", UUID.randomUUID().toString())
        subject.put("username", "frothy@example.com", UUID.randomUUID().toString())

        when:
        def results = subject.getByKeyValue("email", "frothy@example.com")

        then:
        assert results instanceof List
        assert results.size() == 1
        results.each { e -> assert e.key == "email" && e.value == "frothy@example.com" }
        assert !results.find { e -> e.key ==  "name" || e.key == "username" }
    }

    def "overwrites data with matching criteria"() {
        given:
        subject.put("email", "frothy@example.com", "4")

        when:
        def resultsBefore = subject.getByKey("email")
        subject.put("email", "bubbly@example.com", "4")
        def resultsAfter = subject.getByKey("email")

        then:
        assert resultsBefore.size() == 1
        assert resultsAfter.size() == 1
        assert resultsBefore.first().value == "frothy@example.com"
        assert resultsAfter.first().value == "bubbly@example.com"
    }

    def "allows me to delete all data with a matching groupId"() {
        given:
        def groupIdToDelete = UUID.randomUUID().toString();
        def groupIdToKeep = UUID.randomUUID().toString();
        subject.put("name", "Joey McJoe", groupIdToKeep)
        subject.put("email", "frothy@example.com", groupIdToKeep)
        subject.put("username", "McJ!", groupIdToKeep)
        subject.put("name", "Abra Cadabra", groupIdToDelete)
        subject.put("email", "mystic@example.com", groupIdToDelete)
        subject.put("username", "alakazam", groupIdToDelete)
        assert subject.getStore().size() == 6

        when:
        subject.deleteByGroupId(groupIdToDelete)

        then:
        assert subject.getStore().size() == 3
        assert subject.getStore().findAll { e -> e.getKey().split("/")[0] == groupIdToKeep }.size() == 3
    }

    def "deleteByGroupId does basic validation"() {
        given:
        Map<String, String> mockStore = Mock(Map)
        subject.setStore(mockStore)

        when:
        subject.deleteByGroupId(null)
        subject.deleteByGroupId("")

        then:
        0 * mockStore._
    }

    def "returns an empty list when nothing is found"() {
        when:
        List<MemoryStorageModel> resultGetByGroupId = subject.getByGroupId("humma")
        List<MemoryStorageModel> resultGetByKey = subject.getByKey("yadda")
        List<MemoryStorageModel> resultGetByValue = subject.getByValue("diddle")
        List<MemoryStorageModel> resultGetByKeyValue = subject.getByKeyValue("blah", "etcetera")

        then:
        assert resultGetByGroupId instanceof List
        assert resultGetByGroupId.size() == 0
        assert resultGetByKey instanceof List
        assert resultGetByKey.size() == 0
        assert resultGetByValue instanceof List
        assert resultGetByValue.size() == 0
        assert resultGetByKeyValue instanceof List
        assert resultGetByKeyValue.size() == 0
    }
}
