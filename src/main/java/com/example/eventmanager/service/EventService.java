package com.example.eventmanager.service;


import com.example.eventmanager.dao.EventRepository;
import com.example.eventmanager.domain.CalendarData;
import com.example.eventmanager.domain.Category;
import com.example.eventmanager.domain.Event;
import com.example.eventmanager.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;

    @Autowired
    public EventService(EventRepository eventRepository, UserService userService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
    }


    public void createEvent(Event event){
        event.setId((long) eventRepository.save(event));
        eventRepository.addUserToEvent(userService.getCurrentUser().getId(),event.getId());
    }


    public void updateEvent(Event event){
        eventRepository.update(event);
    }

    public Event getEvent (Long id){
        return eventRepository.findOne(id);
    }

    public List<Event> getUserEvents(){

       return eventRepository.findAllUserEvents(userService.getCurrentUser().getId());
    }

    public List<Event> getEventsWithUserParticipation(Long userId,Boolean isPrivate,Boolean isSent){

        return eventRepository.findEventsWithUserParticipation(userId, isPrivate, isSent);
    }

    public List<Event> getAllPublicEvents(){

        return eventRepository.findAllPublicEvents();
    }

    public List<Event> eventsForPeriod(Long user_id,LocalDate fromDate, LocalDate toDate){

        return eventRepository.eventsForPeriod(user_id,fromDate,toDate);
    }

    public void joinToEvent(Long event_id){

        eventRepository.addUserToEvent(userService.getCurrentUser().getId(),event_id);

    }

    public void AddUsersToEvent(List<User> users,Long id){

        for (User user:users) {
            eventRepository.addUserToEvent(user.getId(),id);
        }
    }

    public List<User> getParticipants(Long id){

        return eventRepository.findParticipants(id);
    }

    public void deleteEvent(Event event){
        eventRepository.delete(event);
    }

    public String getPriority(Long event_id){

        return eventRepository.getPriority(userService.getCurrentUser().getId(),event_id);
    }

    public void changePriority(Long event_id, Long priority_id){

        eventRepository.changePriority(userService.getCurrentUser().getId(),event_id,priority_id);
    }

    public boolean isParticipant(Long event_id){

        return eventRepository.isParticipant(userService.getCurrentUser().getId(),event_id);
    }

    public void leaveEvent(Long event_id){

        eventRepository.deleteParticipant(userService.getCurrentUser().getId(),event_id);
    }

    public Long countSearchResults(String pattern, String category, LocalDateTime start, LocalDateTime finish) {
        return eventRepository.countSearchResults(pattern, start, finish, category);
    }

    public List<Event> searchWithFiltersPagination(String pattern, String category, LocalDateTime start, LocalDateTime finish,
                                                   Long limit, Long offset) {
        return eventRepository.searchWithFiltersPagination(pattern, start, finish, category, limit, offset);
    }

    public List<Event> searchUserEventsWithFiltersPagination(String pattern, LocalDateTime start, LocalDateTime finish,
                                                             String category, Long userId, Long priority, Boolean byPriority,
                                                             Boolean privat, Long limit, Long offset) {
        return eventRepository.searchUserEventsWithFiltersPagination(pattern, start, finish, category,
                userId, priority, byPriority, privat, limit, offset);
    }

    public Long countSearchUserEventsResults(String pattern, LocalDateTime start, LocalDateTime finish,
                                                             String category, Long userId, Long priority, Boolean byPriority,
                                                             Boolean privat) {
        return eventRepository.countSearchUserEventsResults(pattern, start, finish, category,
                userId, priority, byPriority, privat);
    }

    public List<Category> getCategories(){

       return eventRepository.getCategories();
    }

    public Map<Integer, List<Integer>> getCalendarCounts(LocalDateTime start, LocalDateTime finish,
                                                         Long user_id, Boolean privat) {
        List<CalendarData> dataList = eventRepository.getCalendarData(start, finish, user_id, privat);
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (LocalDateTime current = start; current.isBefore(finish); current = current.plusDays(1)) {
            LocalDateTime currentStart = current;
            LocalDateTime currentFinish = current.plusDays(1);
            List<CalendarData> dataByDay = dataList.stream()
                    .filter(event -> event.getStart().isBefore(currentFinish))
                    .filter(event -> event.getFinish().isAfter(currentStart))
                    .collect(Collectors.toList());
            result.put(current.getDayOfMonth(), Arrays.asList(
                    (int) dataByDay.stream().filter(event -> event.getPriority() == 0L).count(),
                    (int) dataByDay.stream().filter(event -> event.getPriority() == 1L).count(),
                    (int) dataByDay.stream().filter(event -> event.getPriority() == 2L).count(),
                    (int) dataByDay.stream().filter(event -> event.getPriority() == 3L).count()
            ));
        }
        return result;
    }
}
