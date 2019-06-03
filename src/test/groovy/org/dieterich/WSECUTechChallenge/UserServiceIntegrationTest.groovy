package org.dieterich.WSECUTechChallenge

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

    def "createUser throws an exception if there is a taken username"() {
        given:
        def storage = MemoryStorage.getInstance()
        storage.put("username", "kaboom!", UUID.randomUUID().toString())

        when:
        subject.createUser("kaboom!", "whatever", "does not matter")

        then:
        thrown(DuplicateUserException)
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
}
