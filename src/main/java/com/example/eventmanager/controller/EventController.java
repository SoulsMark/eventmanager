package com.example.eventmanager.controller;

import com.example.eventmanager.domain.Event;
import com.example.eventmanager.domain.EventView;
import com.example.eventmanager.domain.User;
import com.example.eventmanager.service.EventService;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(value = "/event")
public class EventController {

    private final EventService eventService;
    private final Logger logger = LogManager.getLogger(EventController.class);

    @Autowired
    public EventController(EventService eventService) {
        logger.info("Class initialized");

        this.eventService = eventService;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<Void> createEvent(@RequestBody Event event, UriComponentsBuilder ucBuilder) {
        logger.info("POST /");

        eventService.createEvent(event);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/event/{id}").buildAndExpand(event.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @JsonView(EventView.ShortView.class)
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> listAllPublicEvent() {
        logger.info("GET /");

        List<Event> events = eventService.getAllPublicEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @JsonView(EventView.FullView.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Event> getEvent(@PathVariable("id") Long id) {
        logger.info("GET /" + id);

        Event event = eventService.getEvent(id);
        if (event == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @JsonView(EventView.FullView.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<Event> updateEvent(@PathVariable("id") Long id, @RequestBody Event newEvent) {
        logger.info("POST /" + id);

        Event oldEvent = eventService.getEvent(id);
        if (oldEvent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        newEvent.setId(oldEvent.getId());
        newEvent.setCreator(oldEvent.getCreator());
        eventService.updateEvent(newEvent);
        return new ResponseEntity<>(newEvent, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Event> deleteEvent(@PathVariable("id") Long id) {
        logger.info("DELETE /" + id);

        Event event = eventService.getEvent(id);
        if (event == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        eventService.deleteEvent(event);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @JsonView(EventView.FullView.class)
    @RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getParticipants(@PathVariable Long id) {
        logger.info("GET /" + id + "/participants");

        List<User> userList = eventService.getParticipants(id);
        return new ResponseEntity<>(userList, HttpStatus.OK);
    }

    //    TODO: Refactor methods which below
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public void publishEvent(@RequestBody Event event) {
        logger.info("POST /publish");

        eventService.publishEvent(event);
    }

    @RequestMapping(value = "/withUserParticipation", method = RequestMethod.POST)
    public List<Event> withUserParticipation(@RequestBody User user) {
        logger.info("POST /withUserParticipation");

        return eventService.getEventsWithUserParticipation(user);
    }

    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public void publish(@RequestBody User user, Event event) {
        logger.info("POST /join");

        eventService.joinToEvent(user, event);
    }

    @RequestMapping(value = "/addParticipants", method = RequestMethod.POST)
    public void addParticipants(@RequestBody List<User> users, Event event) {
        logger.info("POST /addParticipants");

        eventService.AddUsersToEvent(users, event);
    }

}
