package org.dieterich.WSECUTechChallenge

import org.dieterich.WSECUTechChallenge.Controllers.UserController
import org.dieterich.WSECUTechChallenge.DataAccess.UserService
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage
import org.dieterich.WSECUTechChallenge.Exceptions.DuplicateUserException
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException
import org.dieterich.WSECUTechChallenge.Models.User
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

class UserControllerUnitTest extends Specification {
    UserService mockUserService
    UserController subject

    def setup() {
        subject = new UserController()
        subject.metaClass.static.setService = { newObj ->
            userService = newObj
        }
        mockUserService = Mock(UserService)
        subject.setService(mockUserService)
    }

    def "should return an existing user by username"() {
        given:
        User fakeUser = new User(id: UUID.randomUUID().toString(), name: "name", username: "username", email: "email@example.com")
        def userMatches = { e, a ->
            e.id == a.id &&
                    e.username == a.username &&
                    e.name == a.name &&
                    e.email == a.email
        }

        when:
        def result = subject.getUserByUsername("username")

        then:
        1 * mockUserService.getUserByUsername("username") >> fakeUser
        assert userMatches(fakeUser, result)
    }

    def "should return an existing user by userId"() {
        given:
        User fakeUser = new User(id: UUID.randomUUID().toString(), name: "name", username: "username", email: "email@example.com")
        def userMatches = { e, a ->
            e.id == a.id &&
                    e.username == a.username &&
                    e.name == a.name &&
                    e.email == a.email
        }

        when:
        def result = subject.getUserById(fakeUser.id)

        then:
        1 * mockUserService.getUserById(fakeUser.id) >> fakeUser
        assert userMatches(fakeUser, result)
    }

    def "should bubble up 'original' exceptions"() {
        when:
        def result = subject.getUserByUsername("username")

        then:
        1 * mockUserService.getUserByUsername("username") >> { throw(new NothingFoundException("KABOOM!")) }
        NothingFoundException nfe = thrown(NothingFoundException)
        assert nfe.message == "KABOOM!"
    }

    def "handleNotFoundException should return an error object that can be rendered nicely"() {
        when:
        def result = subject.handleNotFoundException(new NothingFoundException("KABOOM!"))

        then:
        assert result instanceof UserController.UserError
        assert result.statusString == "Not Found"
        assert result.errorMessage == "Cannot find any matching user"
    }

    def "handleDuplicateUserException should return an error object that can be rendered nicely"() {
        when:
        def result = subject.handleDuplicateUserException(new DuplicateUserException("KABOOM!"))

        then:
        assert result instanceof UserController.UserError
        assert result.statusString == "Unable to complete"
        assert result.errorMessage == "User data provided must be unique"
    }

    def "allows me to create a new user"() {
        given:
        def userData = new User( email: "email", name: "name", username: "username")
        def userMatches = { e, a ->
            e.id != a.id &&
                    e.username == a.username &&
                    e.name == a.name &&
                    e.email == a.email
        }

        when:
        def resultCreate = subject.createUser(userData)

        then:
        1 * mockUserService.createUser("username", "name", "email") >>
                new User(id: "uuid", email: "email", name: "name", username: "username")
        userMatches(resultCreate, userData)
    }

    def "allows me to update an existing user"() {
        given:
        def userData = new User( id: "uuid", email: "email", name: "name", username: "username")
        def userMatches = { e, a ->
            e.id == a.id &&
                    e.username == a.username &&
                    e.name == a.name &&
                    e.email == a.email
        }

        when:
        def resultCreate = subject.updateUser(userData)

        then:
        1 * mockUserService.updateUser(userData)
        1 * mockUserService.getUserById(userData.id) >> userData
        userMatches(resultCreate, userData)
    }
}
