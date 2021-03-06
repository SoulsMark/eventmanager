import {Component, Input, OnInit} from '@angular/core';
import { WishListService } from '../../services/wishlist.service';
import { HttpClient } from '@angular/common/http';
import {Router, ActivatedRoute} from '@angular/router';
import { JQueryStatic } from 'jquery';


import { Event } from '../../model/event'
import {Item} from '../../model/item';
import {Subscription} from 'rxjs/Subscription';
import {LikeService} from "../../services/like.service";
import {ItemService} from "../../services/item.service";
import {WishList} from "../../model/wishlist";
import {AuthService} from "../../services/auth.service";
import {User} from "../../model/user";
import {UserService} from "../../services/user.service";
import {EventService} from "../../services/event.service";

declare var $:JQueryStatic;

@Component({
  selector: 'app-wish-list',
  templateUrl: './wishlist.component.html',
  styleUrls: ['./wishlist.component.css']
})
export class WishListComponent implements OnInit {

  wishList: WishList;
  title = 'Your Wish Board!';
  items: Item[] = [];
  subscription: Subscription;
  isOwn: boolean = false;
  isLoaded: boolean = false;
  isPopular: boolean = false;
  isBooking: boolean = false;
  isSuggested: boolean = false;
  user: User = new User();

  @Input()
  private eventId: number;
  @Input()
  private userId: number;
  @Input()
  private isEventPanel: boolean;


  constructor(private route: ActivatedRoute,
              private router: Router,
              private wishListService: WishListService,
              private likeService: LikeService,
              private itemService: ItemService,
              private auth: AuthService,
              private userService: UserService,
              ) {

  }

  ngOnInit() {

	this.items = [];



	if (this.router.url == "/items/popular"){

	  this.isPopular = true;
      this.initPopularItems()

	} else if (this.router.url == "/items/suggestions") {

		this.isSuggested = true;
		this.initSuggestionItems();

	} else if (this.router.url == '/items/booking') {

		this.isBooking = true;
		this.initBookedItems();

	} else if (this.router.url == '/wishlist') {

	    this.isOwn = true;
      this.auth.getUser().subscribe((user: User) => {
        this.initWishList(user.id);
      });

	} else if (this.userId && this.eventId && this.isEventPanel){
		this.initEventsBookedItems();

	} else {

		this.route.params.subscribe(params => {

        let id: number = params['userId'];

		if(!id) {
			id = this.userId;
		}

        if (id) {
          this.userService.getUserById(id).subscribe((user: User) => {
            if (user) {
              this.user = user;
              this.initWishList(user.id)
            } else {
              console.log(`User with id '${id}' not found!`);
            }
          });
        }


      });

    }

  }

  public initWishList(userId: number): void{
    this.wishListService.getWishListByUser(userId).subscribe(


      ( wishList: WishList ) => {
        if (wishList != null) {
          this.wishList = wishList;
          console.log(this.wishList);


          this.wishListService.getItemsFromWishList(this.wishList.id)
            .subscribe((items: any) => {
              this.items = items;
              for( let item of this.items) {
                this.likeService.wasLiked(item.id).subscribe( (hasLike: boolean) => {
                  item.hasLiked = hasLike;
                });
              }
			        this.isLoaded = true;
            });


          this.subscription = this.wishListService.getViewingItem().subscribe(item => {});

        }


      }

    )
  }

  initPopularItems() {
    this.wishListService.getPopularItems(20, 0).subscribe(
      (items: Item[]) => {
        this.items = items;
        for( let item of this.items) {
          this.likeService.wasLiked(item.id).subscribe( (hasLike: boolean) => {
            item.hasLiked = hasLike;
          });
        }
        this.isLoaded = true;
      }
    );

    this.subscription = this.wishListService.getViewingItem().subscribe(item => {});
  }

  initSuggestionItems() {
    this.wishListService.getSuggestionItems(20).subscribe(
      (items: Item[]) => {
        this.items = items;
        for( let item of this.items) {
          this.likeService.wasLiked(item.id).subscribe( (hasLike: boolean) => {
            item.hasLiked = hasLike;
          });
        }
        this.isLoaded = true;
      }
    );

    this.subscription = this.wishListService.getViewingItem().subscribe(item => {});
  }

   initBookedItems() {
	this.auth.getUser().subscribe((user: User) => {

		this.wishListService.getBookedItems(user.id).subscribe(
		  (items: Item[]) => {
			this.items = items;
			for( let item of this.items) {
			  this.likeService.wasLiked(item.id).subscribe( (hasLike: boolean) => {
				item.hasLiked = hasLike;
			  });
			}
			this.isLoaded = true;
		  }
		);
	});

    this.subscription = this.wishListService.getViewingItem().subscribe(item => {});
  }

  public initEventsBookedItems() {

	this.wishListService.getEventsBookedItems(this.eventId).subscribe(
		(items: Item[]) => {
      this.items = items;
      for( let item of this.items) {
        this.likeService.wasLiked(item.id).subscribe( (hasLike: boolean) => {
          item.hasLiked = hasLike;
        });
      }
			this.isLoaded = true;
		}
	);

	this.subscription = this.wishListService.getViewingItem().subscribe(item => {});

  }



  create() {
  }

  sendViewingItem(item: Item){

    this.itemService.getItem(item.id).subscribe( (fullItem: Item) => {
      console.log(fullItem);
      this.wishListService.sendViewingItem(fullItem)
    } );
  }

  hideViewingItem(){
    this.wishListService.hideViewingItem()
  }

  hideViewAndShowList(){
	$('#viewItem').modal('toggle');
  }

  clickOnLikeButt(item: Item){
    this.likeService.addLike(item);
  }

  removeItem (item: Item) {
    this.itemService.deleteItem(item);
  }

  ngOnDestroy(): void {
	if(this.subscription) {
		this.subscription.unsubscribe();
	}
  }

  isOwnBoard(): boolean {
    return this.isOwn;
  }

  isPopularBoard(): boolean {
    return this.isPopular;
  }

  getEventId() : number {
    return this.eventId;
  }


}
