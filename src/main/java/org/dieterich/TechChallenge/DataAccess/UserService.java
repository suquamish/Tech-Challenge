package org.dieterich.TechChallenge.DataAccess;

import org.dieterich.TechChallenge.DataStorage.MemoryStorage;
import org.dieterich.TechChallenge.Exceptions.DuplicateUserException;
import org.dieterich.TechChallenge.Exceptions.NothingFoundException;
import org.dieterich.TechChallenge.Models.MemoryStorageModel;
import org.dieterich.TechChallenge.Models.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private MemoryStorage dataStorage;

    private final String USERNAME_KEY = "username";
    private final String EMAIL_KEY = "email";
    private final String NAME_KEY = "name";

    public UserService() {
         dataStorage = MemoryStorage.getInstance();
    }

    public User createUser(String username, String name, String email) throws DuplicateUserException {
        User result;
        if (usernameExists(username)) throw new DuplicateUserException("${username} already exists");
        String userId = dataStorage.put(USERNAME_KEY, username).get(0).getGroupId();
        dataStorage.put(EMAIL_KEY, email, userId);
        dataStorage.put(NAME_KEY, name, userId);
        result = new User().setUsername(username).setEmail(email).setName(name).setId(userId);
        return result;
    }

    public User getUserById(String groupId) throws NothingFoundException {
        List<MemoryStorageModel> userInfo = dataStorage.getByGroupId(groupId);
        if (userInfo.size() > 0) {
            return userFromMemoryModel(userInfo);
        }
        throw (new NothingFoundException(String.format("no user with id \"%s\"", groupId)));
    }

    public User getUserByUsername(String username) throws NothingFoundException {
        if(usernameExists(username)) {
            String userId = dataStorage.getByKeyValue(USERNAME_KEY, username).get(0).getGroupId();
            return userFromMemoryModel(dataStorage.getByGroupId(userId));
        }
        throw new NothingFoundException("${username} does not exist");
    }

    public void updateUser(User user) throws NothingFoundException, DuplicateUserException {
        User previousUserData = getUserById(user.getId());
        String userId = user.getId();
        validateUpdatedUserData(user);

        if(!user.getUsername().equals(previousUserData.getUsername()))dataStorage.put(USERNAME_KEY, user.getUsername(), userId);
        if(!user.getName().equals(previousUserData.getName())) dataStorage.put(NAME_KEY, user.getName(), userId);
        if(!user.getEmail().equals(previousUserData.getEmail())) dataStorage.put(EMAIL_KEY, user.getEmail(), userId);
    }

    public void deleteUserById(String id) {
        if (id == null || id.isEmpty()) return;

        dataStorage.deleteByGroupId(id);
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

    private boolean usernameExists(String username) {
        List<MemoryStorageModel> userInfo = dataStorage.getByKeyValue(USERNAME_KEY, username);
        return userInfo.size() > 0;
    }

    private void validateUpdatedUserData(User user) throws DuplicateUserException {
        validateNoUsernameCollision(user);
        //validateEmailAddress(user);
        //validateNoNameForgery(user);
        //etc(user);
    }

    private void validateNoUsernameCollision(User user) throws DuplicateUserException {
        try {
            User userByUsername = getUserByUsername(user.getUsername());
            if (!userByUsername.getId().equals(user.getId())) {
                throw (new DuplicateUserException("Cannot update user, username already exists"));
            }
        } catch(NothingFoundException nfe) {}
    }
}
