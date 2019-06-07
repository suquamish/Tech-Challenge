package org.dieterich.TechChallenge


import org.dieterich.TechChallenge.DataAccess.UserService
import org.dieterich.TechChallenge.DataStorage.MemoryStorage
import org.dieterich.TechChallenge.Exceptions.DuplicateUserException
import org.dieterich.TechChallenge.Exceptions.NothingFoundException
import org.dieterich.TechChallenge.Models.User
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
        def groupId = storage.put("name", "Joey McJoe").first().groupId
        storage.put("email", "joey.mcjoe@example.com", groupId)
        storage.put("username", "kaboom!", groupId)
        storage.put("username", "w00tw00t")

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
        storage.put("username", "kaboom!")

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

    def "updating a user changes an existing users data"() {
        given:
        def user = subject.createUser("joe.user", "Joe User", "email@example.com")
        def checkUser = subject.getUserById(user.id)
        def userMatches = { e, a ->
            e.id == a.id &&
                    e.username == a.username &&
                    e.name == a.name &&
                    e.email == a.email
        }
        assert userMatches(checkUser, user)
        user.email = "joe.user@example.com"
        user.name = "Mr. Joe User"
        assert !userMatches(checkUser, user)

        when:
        subject.updateUser(user)
        checkUser = subject.getUserById(checkUser.id)

        then:
        assert userMatches(checkUser, user)
        noExceptionThrown()
    }
}
