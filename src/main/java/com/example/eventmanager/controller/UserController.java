package com.example.eventmanager.controller;

import com.example.eventmanager.domain.Event;
import com.example.eventmanager.domain.EventView;
import com.example.eventmanager.domain.User;
import com.example.eventmanager.domain.UserView;
import com.example.eventmanager.service.EmailService;
import com.example.eventmanager.service.EventService;
import com.example.eventmanager.service.SecurityService;
import com.example.eventmanager.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;
    private final EmailService emailService;
    private final EventService eventService;
    private final Logger logger = LogManager.getLogger(UserController.class);

    private static final int NO_RELATIONSHIP = -1;
    private static final int PENDING = 0;
    private static final int ACCEPTED = 1;

    @Autowired
    public UserController(UserService userService, SecurityService securityService, EmailService emailService,
                          EventService eventService) {
        logger.info("Class initialized");

        this.userService = userService;
        this.securityService = securityService;
        this.emailService = emailService;
        this.eventService = eventService;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void create(@RequestBody User user) {
        logger.info("POST /");

        userService.saveUser(
                securityService.encodePass(
                        securityService.verificationToken(user)));
        emailService.sendTextMail(
                user.getEmail(),
                "Email verification",
                "Please verify your email:\n" +
                        "https://web-event-manager.firebaseapp.com/email-verification/" +
                        user.getToken());
    }

//    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
//    public void update(@PathVariable("id") Long id, @RequestBody User user) {
//        logger.info("PATCH /");
//
//        userService.updateUser(user);
//    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User newUser) {
        logger.info("PUT /" + id);
        System.out.println("newUser - " + newUser.toString());
        User oldUser = userService.getUser(id);
        if (oldUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        newUser.setId(oldUser.getId());
        userService.updateUser(newUser);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/changePassword", params = {"oldPassword", "newPassword"}, method = RequestMethod.PUT)
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestParam String oldPassword, @RequestParam String newPassword) {
        logger.info("PUT /{id}/changePassword", id);

        User user = userService.getUser(id);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if(securityService.comparePass(user, oldPassword)){
            user.setPassword(newPassword);
            userService.changePass(securityService.encodePass(user));
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/{id}/changeEmail", params = {"oldEmail", "newEmail"}, method = RequestMethod.PUT)
    public ResponseEntity<Void> changeEmail(@PathVariable Long id, @RequestParam String oldEmail, @RequestParam String newEmail) {
        logger.info("PUT /{id}/changeEmail", id);

        User user = userService.getUser(id);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if(oldEmail.equals(user.getEmail())){
            securityService.verificationToken(user);
            userService.changeToken(user);

            String verificationLink;
            try {
                verificationLink = "https://web-event-manager.firebaseapp.com/email-verification/" +
                        user.getToken() + "?email=" + URLEncoder.encode(newEmail, "UTF-8");
                emailService.sendTextMail(
                        newEmail,
                        "Confirm email changing",
                        "Please verify your new email:\n" +
                                verificationLink);
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/by-username/{username}", method = RequestMethod.GET)
    public User get(@PathVariable String username) {
        logger.info("GET /" + username);

        return userService.findUser(username);
    }

    @RequestMapping(value = "/exists/username/{username}", method = RequestMethod.GET)
    public boolean existsUsername(@PathVariable String username) {
        logger.info("GET /exists/username/" + username);

        return userService.isUsernameExists(username);
    }

    @RequestMapping(value = "/exists/email/{email}", method = RequestMethod.GET)
    public boolean existsEmail(@PathVariable String email) {
        logger.info("GET /exists/email/" + email);

        return userService.isEmailExists(email);
    }

    @JsonView(UserView.ShortView.class)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity<List<User>> listAllUsers() {
        logger.info("GET /all");

        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @JsonView(UserView.ShortView.class)
    @RequestMapping(value = "/all", params = {"limit", "offset"}, method = RequestMethod.GET)
    public ResponseEntity<List<User>> getAllUsersPagination(
            @RequestParam int limit, @RequestParam int offset, HttpServletResponse response) {
        logger.info("GET /users/all?limit={}&offset={}", limit, offset);

        response.addHeader("count", userService.countAllUsers().toString());
        List<User> users = userService.getAllUsersPagination(limit, offset);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @JsonView(UserView.FullView.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        logger.info("GET /" + id);

        User user = userService.getUser(id);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @JsonView(EventView.FullView.class)
    @RequestMapping(value = "/{id}/events", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getUserEvents(@PathVariable Long id,@RequestParam Boolean isPrivate,@RequestParam Boolean isSent) {
        logger.info("GET /" + id + "/events");

        List<Event> eventList = eventService.getEventsWithUserParticipation(id, isPrivate, isSent);
        return new ResponseEntity<>(eventList, HttpStatus.OK);
    }
    @JsonView(EventView.FullView.class)
    @RequestMapping(value = "/{id}/myevents", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getCurrentUserEvents(@PathVariable Long id) {
        logger.info("GET /" +id+ "/myevents");

        List<Event> eventList = eventService.getUserEvents();
        return new ResponseEntity<>(eventList, HttpStatus.OK);
    }

    @JsonView(UserView.ShortView.class)
    @RequestMapping(value = "/search", params = "query", method = RequestMethod.GET)
    public ResponseEntity<List<User>> searchByLoginOrByNameAndSurname(@RequestParam("query") String queryString) {
        logger.info("GET /users/search?query=" + queryString);

        List<User> users = userService.searchByLoginOrByNameAndSurname(queryString);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @JsonView(UserView.ShortView.class)
    @RequestMapping(value = "/search", params = {"query", "limit", "offset"}, method = RequestMethod.GET)
    public ResponseEntity<List<User>> searchByLoginOrByNameAndSurnamePagination(
            @RequestParam String query, @RequestParam int limit, @RequestParam int offset, HttpServletResponse response) {
        logger.info("GET /users/search?query={}&limit={}&offset={}", query, limit, offset);
        List<User> users;

        if(query.equals("")){
            response.addHeader("count", userService.countAllUsers().toString());
            users = userService.getAllUsersPagination(limit, offset);
        } else {
            response.addHeader("count", userService.countSearchByLoginOrByNameAndSurname(query).toString());
            users = userService.searchByLoginOrByNameAndSurnamePagination(query, limit, offset);
        }

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    //Friends functionality

    @RequestMapping(value = "/addFriendRequest", params = {"from", "to"}, method = RequestMethod.POST)
    public ResponseEntity<String> addFriendRequest(@RequestParam Long from, @RequestParam Long to) {
        logger.info("POST /addFriendRequest");

        if(userService.getRelationshipStatusId(from, to) == NO_RELATIONSHIP){
            userService.saveRelationship(from, to, PENDING, from);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            logger.info("Users {} and {} already have some relationship", from, to);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/answerFriendRequest", params = {"requester", "accepter", "accept"},
            method = RequestMethod.PUT)
    public ResponseEntity<String> answerFriendRequest(@RequestParam Long requester, @RequestParam Long accepter,
                                                      @RequestParam Boolean accept) {
        logger.info("PUT /answerFriendRequest");

        if(userService.getRelationshipStatusId(requester, accepter) == PENDING){
            if (accept) {
                userService.updateRelationship(requester, accepter, ACCEPTED, accepter);
            } else {
                userService.deleteRelationship(requester, accepter);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            logger.info("There is no request for friendship from user {} or user {} or they have other relationship",
                    requester, accepter);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/deleteRelationship", params = {"from", "to"}, method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteRelationship(@RequestParam Long from, @RequestParam Long to) {
        logger.info("DELETE /deleteRelationship");

        int usersRelationship = userService.getRelationshipStatusId(from, to);
        if(usersRelationship == PENDING || usersRelationship == ACCEPTED){
            userService.deleteRelationship(from, to);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            logger.info("User {} and user {} was not friends", from, to);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/getRelationshipStatus", params = {"from", "to"}, method = RequestMethod.GET)
    public String getRelationshipStatus(@RequestParam Long from, @RequestParam Long to) {
        logger.info("GET /getRelationshipStatus");
        return userService.getRelationshipStatus(from, to);
    }

    @RequestMapping(value = "/getRelationshipStatusId", params = {"from", "to"}, method = RequestMethod.GET)
    public int getRelationshipStatusId(@RequestParam Long from, @RequestParam Long to) {
        logger.info("GET /getRelationshipStatusId");
        return userService.getRelationshipStatusId(from, to);
    }

    @RequestMapping(value = "/getRelationshipStatusAndActiveUserId", params = {"from", "to"}, method = RequestMethod.GET)
    public Map<String, Object> getRelationshipStatusAndActiveUserId(@RequestParam Long from, @RequestParam Long to) {
        logger.info("GET /getRelationshipStatusAndActiveUserId");
        return userService.getRelationshipStatusAndActiveUserId(from, to);
    }

    @RequestMapping(value = "/getRelationshipStatusIdAndActiveUserId", params = {"from", "to"}, method = RequestMethod.GET)
    public Map<String, Object> getRelationshipStatusIdAndActiveUserId(@RequestParam Long from, @RequestParam Long to) {
        logger.info("GET /getRelationshipStatusIdAndActiveUserId");
        return userService.getRelationshipStatusIdAndActiveUserId(from, to);
    }

    @RequestMapping(value = "/{id}/outcomingRequests", method = RequestMethod.GET)
    public List<User> getOutcomingRequests(@PathVariable(value = "id") Long userId) {
        logger.info("GET /{}/outcomingRequests", userId);
        return userService.getOutcomingRequests(userId);
    }

    @RequestMapping(value = "/{id}/incomingRequests", method = RequestMethod.GET)
    public List<User> getIncomingRequests(@PathVariable(value = "id") Long userId) {
        logger.info("GET /{}/incomingRequests", userId);
        return userService.getIncomingRequests(userId);
    }

    @RequestMapping(value = "/{id}/friends", method = RequestMethod.GET)
    public List<User> getFriendsByUserId(@PathVariable(value = "id") Long userId) {
        logger.info("GET /{}/friends", userId);
        return userService.getFriendsByUserId(userId);
    }

}
