package org.dieterich.WSECUTechChallenge

import org.dieterich.WSECUTechChallenge.DataAccess.UserService
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorageModel
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException
import org.dieterich.WSECUTechChallenge.Exceptions.DuplicateUserException
import org.dieterich.WSECUTechChallenge.Models.User
import spock.lang.Specification
import spock.lang.Unroll

class UserServiceUnitTest extends Specification {
    UserService subject
    MemoryStorage mockMemoryStorage

    def setup() {
        subject = new UserService();
        subject.metaClass.static.setStore = { newObj ->
            dataStorage = newObj
        }
        mockMemoryStorage = Mock(MemoryStorage)
        subject.setStore(mockMemoryStorage)
    }

    def "createUser doesn't allow duplicate usernames"() {
        given:
        List<MemoryStorageModel> mockData = new ArrayList<>()
        mockData.add(new MemoryStorageModel(key: "username", value: "kaboom!", groupId: UUID.randomUUID().toString()))

        when:
        subject.createUser("kaboom!", "new user", "new.user@example.com")

        then:
        1 * mockMemoryStorage.getByKeyValue("username", "kaboom!") >> mockData
        thrown(DuplicateUserException)
    }

    def "createUser allows me to insert a user into the datastore"() {
        given:
        List<MemoryStorageModel> mockData = new ArrayList<>()
        String userId
        def storeUserId = { String id -> userId = id }

        when:
        def result = subject.createUser("newuser", "Newton User", "new.user@example.com")

        then:
        assert result instanceof User
        1 * mockMemoryStorage.getByKeyValue("username", "newuser") >> new ArrayList<MemoryStorageModel>()
        1 * mockMemoryStorage.put(
                "username",
                "newuser",
                { String uuid ->
                    storeUserId(uuid)
                    assert UUID.fromString(uuid)
                }
        )
        1 * mockMemoryStorage.put("email", "new.user@example.com", _)
        1 * mockMemoryStorage.put("name", "Newton User", _)
        assert result.id == userId
        assert result.email == "new.user@example.com"
        assert result.username == "newuser"
        assert result.name == "Newton User"
    }

    def "getUserByGroupID retrieves and existing user by id"() {
        given:
        String userGroupId = UUID.randomUUID().toString()
        List<MemoryStorageModel> mockData = new ArrayList<>();
        mockData.add(new MemoryStorageModel(key: "username", value: "iamauser", groupId: userGroupId))
        mockData.add(new MemoryStorageModel(key: "name", value: "Iam A. User", groupId: userGroupId))
        mockData.add(new MemoryStorageModel(key: "email", value: "iamauser@example.com", groupId: userGroupId))

        when:
        User result = subject.getUserById(userGroupId)

        then:
        1 * mockMemoryStorage.getByGroupId(userGroupId) >> mockData
        assert result.id == userGroupId
        assert result.email == "iamauser@example.com"
        assert result.name == "Iam A. User"
        assert result.username == "iamauser"
    }

    def "getUserByUserName retrieves an existing user by username"() {
        given:
        String userGroupId = UUID.randomUUID().toString();
        List<MemoryStorageModel> keyValueData = new ArrayList<>()
        keyValueData.add(new MemoryStorageModel(key: "username", value: "iamauser", groupId: userGroupId))
        List<MemoryStorageModel> groupIdData = new ArrayList<>()
        groupIdData.add(new MemoryStorageModel(key: "username", value: "iamauser", groupId: userGroupId))
        groupIdData.add(new MemoryStorageModel(key: "name", value: "Iam A. User", groupId: userGroupId))
        groupIdData.add(new MemoryStorageModel(key: "email", value: "iamauser@example.com", groupId: userGroupId))

        when:
        User result = subject.getUserByUsername("iamauser")

        then:
        1 * mockMemoryStorage.getByKeyValue("username", "iamauser") >> keyValueData
        1 * mockMemoryStorage.getByGroupId(userGroupId) >> groupIdData
        assert result.id == userGroupId
        assert result.email == "iamauser@example.com"
        assert result.name == "Iam A. User"
        assert result.username == "iamauser"
    }

    def "getUserByUserName throws NothingFoundException if there is no user"() {
        given:
        List<MemoryStorageModel> emptyList = new LinkedList<>()

        when:
        subject.getUserByUsername("kaboom!")

        then:
        1 * mockMemoryStorage.getByKeyValue("username", "kaboom!") >> emptyList;
        thrown(NothingFoundException)
    }

    def "deleteUserById removes an existing user"() {
        given:
        String userGroupId = UUID.randomUUID().toString();

        when:
        subject.deleteUserById(userGroupId)

        then:
        1 * mockMemoryStorage.deleteByGroupId(userGroupId)
    }

    def "deleteUserById does nothing if you pass in nothing"() {
        when:
        subject.deleteUserById(null)
        subject.deleteUserById("")

        then:
        0 * mockMemoryStorage.deleteByGroupId(_)
    }

    def "deleteUserById does not care if a user exists"() {
        when:
        subject.deleteUserById("really-does-not-matter")

        then:
        1 * mockMemoryStorage.deleteByGroupId("really-does-not-matter")
        noExceptionThrown()
    }

    def "updateUser throws NothingFoundException if you try to update a user that does not exist"() {
        given:
        def userId = UUID.randomUUID().toString()

        when:
        subject.updateUser(new User(id: userId))

        then:
        1 * mockMemoryStorage.getByGroupId(userId) >> new ArrayList<MemoryStorageModel>()
        thrown(NothingFoundException)
    }

    def "updateUser stores the new user info"() {
        given:
        def userId = UUID.randomUUID().toString()
        def fakeUserData = new ArrayList<MemoryStorageModel>()
        fakeUserData.add(new MemoryStorageModel(groupId: userId, key: "key", value: "value"))
        _ * mockMemoryStorage.getByGroupId(userId) >> fakeUserData

        when:
        subject.updateUser(new User(id: userId, email: "email", username: "username", name: "name"))

        then:
        1 * mockMemoryStorage.put("email", "email", userId)
        1 * mockMemoryStorage.put("username", "username", userId)
        1 * mockMemoryStorage.put("name", "name", userId)
    }

    @Unroll
    def "updateUser only updates differing data"() {
        given:
        def userId = UUID.randomUUID().toString()
        def fakeUserData = new ArrayList<MemoryStorageModel>()
        fakeUserData.add(new MemoryStorageModel(groupId: userId, key: "email", value: email))
        fakeUserData.add(new MemoryStorageModel(groupId: userId, key: "username", value: username))
        fakeUserData.add(new MemoryStorageModel(groupId: userId, key: "name", value: name))
        _ * mockMemoryStorage.getByGroupId(userId) >> fakeUserData

        when:
        subject.updateUser(new User(id: userId, email: "email", username: "username", name: "name"))

        then:
        emailCallCount * mockMemoryStorage.put("email", "email", userId)
        usernameCallCount * mockMemoryStorage.put("username", "username", userId)
        nameCallCount * mockMemoryStorage.put("name", "name", userId)

        where:
        email       | name        | username    | emailCallCount | nameCallCount | usernameCallCount
        "email"     | "name"      | "username"  | 0              | 0             | 0
        "different" | "name"      | "username"  | 1              | 0             | 0
        "email"     | "different" | "username"  | 0              | 1             | 0
        "email"     | "name"      | "different" | 0              | 0             | 1
        "different" | "different" | "different" | 1              | 1             | 1
    }
}
