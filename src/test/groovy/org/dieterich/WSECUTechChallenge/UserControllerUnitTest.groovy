package org.dieterich.WSECUTechChallenge

import org.dieterich.WSECUTechChallenge.Controllers.UserController
import org.dieterich.WSECUTechChallenge.DataAccess.UserService
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage
import spock.lang.Specification

class UserControllerUnitTest extends Specification {
    UserService mockUserService
    UserController subject

    def setup() {
        subject = new UserController()
        subject.metaClass.static.setService = { newObj ->
            userService = newObj
        }
        mockUserService = Mock(UserService)
        subject.setStore(mockUserService)
    }

    def "should return an existing user"() {

    }
}
