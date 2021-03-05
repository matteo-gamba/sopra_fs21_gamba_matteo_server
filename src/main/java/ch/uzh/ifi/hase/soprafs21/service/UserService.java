package ch.uzh.ifi.hase.soprafs21.service;
import java.time.LocalDate;

import ch.uzh.ifi.hase.soprafs21.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs21.entity.User;
import ch.uzh.ifi.hase.soprafs21.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired // dependancy injection
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.OFFLINE);
        newUser.setCreationDate(LocalDate.now());

        checkIfUserExists(newUser);

        // saves the given entity but data is only persisted in the database once flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the username and the name
     * defined in the User entity. The method will do nothing if the input is unique and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";

        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
        }
    }

    public User checkIfCredentialsExist(User userInput) {
        User userExists = userRepository.findByUsernameAndPassword(userInput.getUsername(), userInput.getPassword());
        if (userExists == null){
            System.out.println("User does not exist");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The credentials are wrong.");
        }
        else{
            System.out.println("User is logging in");
            this.userRepository.findByUsername(userInput.getUsername()).setStatus(UserStatus.ONLINE);
            User updated_user = this.userRepository.findByUsername(userInput.getUsername());
            System.out.println(this.userRepository.findByUsername(userInput.getUsername()).getStatus());
            return updated_user;
        }
    }

    public User getUserWithId(Long id) {
        Optional<User> user = this.userRepository.findById(id);

        if (user.isPresent()){
            return user.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user with id="+ id +" was not found");
    }

    public User logOut(Long id) {
        var user = this.userRepository.findById(id);

        if (user.isPresent()){
            user.get().setStatus(UserStatus.OFFLINE);
            return this.userRepository.findById(id).get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user with id="+ id +" was not found");
    }

    public User modifyUser(Long id, User userInput) {
        var userToBeModified = this.userRepository.findById(id);
        if (userToBeModified.isPresent()) {
            log.info("User has been modified: {}", userToBeModified);
            userToBeModified.get().setUsername(userInput.getUsername());
            log.info(userInput.getBirthdate().toString());
            userToBeModified.get().setBirthdate(userInput.getBirthdate());
            return userToBeModified.get();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with ID %d was not found.", id));
    }

}
