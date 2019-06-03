package org.dieterich.WSECUTechChallenge.DataAccess;

import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage;
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorageModel;
import org.dieterich.WSECUTechChallenge.Exceptions.DuplicateUserException;
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException;
import org.dieterich.WSECUTechChallenge.Models.User;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private MemoryStorage dataStorage;

    private final String USERNAME_KEY = "username";
    private final String EMAIL_KEY = "email";
    private final String NAME_KEY = "name";

    public UserService() {
         dataStorage = MemoryStorage.getInstance();
    }

    private User userFromMemoryModel(List<MemoryStorageModel> userInfo) {
        User result = new User();
        for(MemoryStorageModel userData : userInfo) {
            result.setId(userData.getGroupId());
            switch (userData.getKey().toLowerCase()) {
                case USERNAME_KEY:
                    result.setUsername(userData.getValue());
                    break;
                case EMAIL_KEY:
                    result.setEmail(userData.getValue());
                    break;
                case NAME_KEY:
                    result.setName(userData.getValue());
                    break;
            }
        }
        return result;
    }

    private boolean userExists(String username) {
        List<MemoryStorageModel> otherUsers = dataStorage.getByKeyValue(USERNAME_KEY, username);
        return otherUsers.size() > 0;
    }

    private String generateUserId() {
        return UUID.randomUUID().toString();
    }

    public User getUserByGroupId(String groupId) throws NothingFoundException {
        List<MemoryStorageModel> userInfo = dataStorage.getByGroupId(groupId);
        if(userInfo.size() > 0) {
            return userFromMemoryModel(userInfo);
        }
        throw(new NothingFoundException(String.format("no user with id \"%s\"", groupId)));
    }

    public User getUserByUsername(String username) throws NothingFoundException {
        User result;
        List<MemoryStorageModel> data = dataStorage.getByKeyValue(USERNAME_KEY, username);
        if(data.isEmpty()) throw new NothingFoundException("${username} does not exist");
        data = dataStorage.getByGroupId(data.get(0).getGroupId());
        result = userFromMemoryModel(data);
        return result;
    }

    public User createUser(String username, String name, String email) throws DuplicateUserException {
        User result = new User();
        if (userExists(username)) throw new DuplicateUserException("${username} already exists");
        result.setId(generateUserId());
        dataStorage.put(USERNAME_KEY, username, result.getId());
        result.setUsername(username);
        dataStorage.put(EMAIL_KEY, email, result.getId());
        result.setEmail(email);
        dataStorage.put(NAME_KEY, name, result.getId());
        result.setName(name);
        return result;
    }

    public void deleteUserById(String id) {
        if (id == null || id.isEmpty()) return;

        dataStorage.deleteByGroupId(id);
    }

    public void updateUser(User user) throws NothingFoundException {
        getUserByGroupId(user.getId());
//        dataStorage.put(USERNAME_KEY, user.getUsername(), user.getId());
//        dataStorage.put(EMAIL_KEY, user.getEmail(), user.getId());
//        dataStorage.put(NAME_KEY, user.getName(), user.getId());
    }
}
