package org.dieterich.WSECUTechChallenge.DataAccess;

import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorage;
import org.dieterich.WSECUTechChallenge.DataStorage.MemoryStorageModel;
import org.dieterich.WSECUTechChallenge.Exceptions.DuplicateUserException;
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException;
import org.dieterich.WSECUTechChallenge.Models.User;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private MemoryStorage dataStorage;

    public final String USERNAME_KEY = "username";
    public final String EMAIL_KEY = "email";
    public final String NAME_KEY = "name";

    public UserService() {
         dataStorage = MemoryStorage.getInstance();
    }

    private boolean usernameMatch(String username, MemoryStorageModel userData) {
        return userData.getKey().toLowerCase().equals(USERNAME_KEY) && userData.getValue().equals(username);
    }

    private User userFromMemoryModel(List<MemoryStorageModel> userInfo) {
        User result = new User();
        for(MemoryStorageModel userData : userInfo) {
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

    public User getUserByGroupId(String groupId) {
        User result = new User();
        List<MemoryStorageModel> userInfo = dataStorage.getByGroupId(groupId);
        if(userInfo.size() > 0) {
            result = userFromMemoryModel(userInfo);
        }
        return result;
    }

    public User getUserByUsername(String username) throws NothingFoundException {
        User result = new User();
        List<MemoryStorageModel> data = dataStorage.getByKeyValue(USERNAME_KEY, username);
        if(data.isEmpty()) throw new NothingFoundException("${username} does not exist");
        for (MemoryStorageModel userData: data) {
            if (usernameMatch(username, userData)) {
                result = getUserByGroupId(userData.getGroupId());
            }
        }
        return result;
    }

    public User createUser(String username, String name, String email) throws DuplicateUserException {
        User result = new User();
        List<MemoryStorageModel> otherUsers = dataStorage.getByKeyValue(USERNAME_KEY, username);
        if(otherUsers.size() > 0) throw new DuplicateUserException("${username} already exists");
        return result;
    }

}
