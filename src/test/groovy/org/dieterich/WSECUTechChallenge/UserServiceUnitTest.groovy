package org.dieterich.WSECUTechChallenge

import org.dieterich.WSECUTechChallenge.DataAccess.UserService
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorageModel
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException
import org.dieterich.WSECUTechChallenge.Exceptions.DuplicateUserException
import org.dieterich.WSECUTechChallenge.Models.User
import spock.lang.Specification

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

    def "getUserByGroupID retrieves and existing user by id"() {
        given:
        String userGroupId = UUID.randomUUID().toString()
        List<MemoryStorageModel> mockData = new ArrayList<>();
        mockData.add(new MemoryStorageModel(key: "username", value: "iamauser", groupId: userGroupId))
        mockData.add(new MemoryStorageModel(key: "name", value: "Iam A. User", groupId: userGroupId))
        mockData.add(new MemoryStorageModel(key: "email", value: "iamauser@example.com", groupId: userGroupId))

        when:
        User result = subject.getUserByGroupId(userGroupId)

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

//    def "updateUser"
}
