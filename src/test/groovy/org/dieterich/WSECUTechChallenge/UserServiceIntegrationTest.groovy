package org.dieterich.WSECUTechChallenge

import com.sun.org.apache.xpath.internal.operations.Bool
import org.dieterich.WSECUTechChallenge.DataAccess.UserService
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage
import org.dieterich.WSECUTechChallenge.Exceptions.DuplicateUserException
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException
import org.dieterich.WSECUTechChallenge.Models.User
import spock.lang.Specification

class UserServiceIntegrationTest extends Specification {
    UserService subject

    def setup() {
        subject = new UserService()
    }

    def "getByUserName throws an exception if there is no user"() {
        when:
        subject.getUserByUsername("kaboom!")

        then:
        thrown(NothingFoundException)
    }

    def "getByUserName returns an existing user"() {
        given:
        def storage = MemoryStorage.getInstance()
        def groupId = UUID.randomUUID().toString()
        storage.put("name", "Joey McJoe", groupId)
        storage.put("email", "joey.mcjoe@example.com", groupId)
        storage.put("username", "kaboom!", groupId)
        storage.put("username", "w00tw00t", UUID.randomUUID().toString())

        when:
        def result = subject.getUserByUsername("kaboom!")

        then:
        result instanceof User
        assert result.username == "kaboom!"
        assert result.email == "joey.mcjoe@example.com"
        assert result.name == "Joey McJoe"
    }

    def "createUser throws an exception if there is a taken username"() {
        given:
        def storage = MemoryStorage.getInstance()
        storage.put("username", "kaboom!", UUID.randomUUID().toString())

        when:
        subject.createUser("kaboom!", "whatever", "does not matter")

        then:
        thrown(DuplicateUserException)
    }

    def "createUser allows me to store a user"() {
        given:
        def storage = MemoryStorage.getInstance()
        storage.metaClass.static.getStore = { store as Map }
        storage.getStore().clear()
        def containsKey = { String key ->
            storage.getStore().keySet().find({k -> k.toString() == key }) as Boolean
        }

        when:
        def result = subject.createUser("w00t", "Willy Vanderw00t", "w00t@example.com")

        then:
        assert storage.getStore().size() == 3
        assert containsKey("${result.id}/email")
        assert containsKey("${result.id}/name")
        assert containsKey("${result.id}/username")
    }

    def "deleteUserId destroys all of a specific users data"() {
        given:
        def storage = MemoryStorage.getInstance()
        storage.metaClass.static.getStore = { store as Map }
        storage.getStore().clear()
        def containsKey = { String key ->
            storage.getStore().keySet().find({k -> k.toString() == key }) as Boolean
        }
        def userToBeDeleted = subject.createUser("byebye", "does not matter", "do not care")
        def userToBeKept = subject.createUser("hello", "whatever", "okay")
        assert storage.getStore().size() == 6

        when:
        subject.deleteUserById(userToBeDeleted.id)

        then:
        assert storage.getStore().size() == 3
        assert containsKey("${userToBeKept.id}/email")
        assert containsKey("${userToBeKept.id}/name")
        assert containsKey("${userToBeKept.id}/username")
    }

    def "deleteUserId does no collateral damage when given bad data"() {
        given:
        def storage = MemoryStorage.getInstance()
        storage.metaClass.static.getStore = { store as Map }
        storage.getStore().clear()
        def containsKey = { String key ->
            storage.getStore().keySet().find({k -> k.toString() == key }) as Boolean
        }
        def userToBeKept = subject.createUser("hello", "whatever", "okay")
        assert storage.getStore().size() == 3

        when:
        subject.deleteUserById(null)

        then:
        assert storage.getStore().size() == 3
        assert containsKey("${userToBeKept.id}/email")
        assert containsKey("${userToBeKept.id}/name")
        assert containsKey("${userToBeKept.id}/username")
    }
}
