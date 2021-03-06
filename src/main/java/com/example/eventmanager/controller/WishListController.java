package com.example.eventmanager.controller;

import com.example.eventmanager.domain.Item;
import com.example.eventmanager.domain.WishList;
import com.example.eventmanager.domain.transfer.view.ItemView;
import com.example.eventmanager.service.ItemService;
import com.example.eventmanager.service.ItemsSuggestionService;
import com.example.eventmanager.service.SecurityService;
import com.example.eventmanager.service.WishListService;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/wishlist")
public class WishListController {
    private final ItemService itemService;
    private final WishListService wishListService;
    private final ItemsSuggestionService itemsSuggestionService;
    private final SecurityService securityService;

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ItemController.class);

    @Autowired
    public WishListController(ItemService itemService, WishListService wishListService,
                              ItemsSuggestionService itemsSuggestionService, SecurityService securityService) {
        this.itemService = itemService;
        this.wishListService = wishListService;
        this.itemsSuggestionService = itemsSuggestionService;
        this.securityService = securityService;

        logger.info("Class initialized");
    }

    @JsonView(ItemView.ShortView.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<Item>> getListOfItems(@PathVariable("id") Long wishListId) {
        logger.info("GET /" + wishListId );

        return new ResponseEntity<>(itemService.getItemsForWishList(wishListId), HttpStatus.OK);
    }

//    @JsonView(ItemView.ShortView.class)
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public ResponseEntity<WishList> getWishListByUser(@PathVariable("id") Long userId) {
        logger.info("GET /user/" + userId );

        return new ResponseEntity<>(wishListService.getWishListByUser(userId), HttpStatus.OK);
    }

    @JsonView(ItemView.ShortView.class)
    @RequestMapping(value = "/popular/items", method = RequestMethod.GET)
    public ResponseEntity<List<Item>> getPopularItems(@RequestParam Long limit, @RequestParam Long offset) {
        logger.info("GET /popular/items");

        return new ResponseEntity<>(itemService.getPopularItems(limit, offset), HttpStatus.OK);
    }

    @JsonView(ItemView.ShortView.class)
    @RequestMapping(value = "/booking/{userId}", method = RequestMethod.GET)
    public ResponseEntity<List<Item>> getBookedItems(@PathVariable("userId") Long userId) {
        logger.info("GET /booking/" + userId);

        return new ResponseEntity<>(itemService.getBookedItems(userId), HttpStatus.OK);
    }

    @JsonView(ItemView.ShortView.class)
    @RequestMapping(value = "/booking/event/{eventId}", method = RequestMethod.GET)
    public ResponseEntity<List<Item>> getEventBookingItems(@PathVariable("eventId") Long eventId) {
        logger.info("GET /booking/event/" + eventId);

        return new ResponseEntity<>(itemService.getEventBookingItems(eventId), HttpStatus.OK);
    }

    @JsonView(ItemView.ShortView.class)
    @RequestMapping(value = "/items/suggestion", method = RequestMethod.GET)
    public ResponseEntity<List<Item>> getSuggestingItems(@RequestParam Long limit) {
        logger.info("GET /items/suggestion");

        return new ResponseEntity<>(itemsSuggestionService.getSuggestingItems(securityService.getCurrentUser().getId(), limit), HttpStatus.OK);
    }
}
