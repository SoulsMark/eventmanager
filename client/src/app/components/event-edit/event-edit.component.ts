import {Component, OnInit, OnDestroy, ViewChild, ElementRef, NgZone} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { Event } from '../../model/event';
import { EventService } from '../../services/event.service';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {CloudinaryUploader} from "ng2-cloudinary";
import {ImageUploaderService} from "../../services/image-uploader.service";
import {AuthService} from "../../services/auth.service";
import {Category} from "../../model/category";
import {imageExtension} from "../../utils/validation-tools";
import {MapsAPILoader} from "@agm/core";
import {} from '@types/googlemaps'

@Component({
  selector: 'app-event-edit',
  templateUrl: './event-edit.component.html',
  styleUrls: ['./event-edit.component.css']
})
export class EventEditComponent implements OnInit, OnDestroy {

  event: Event = new Event();

  uploader: CloudinaryUploader = ImageUploaderService.getUploader();

  public latitude: number;
  public longitude: number;
  public searchControl: FormControl;

  @ViewChild("search")
  public searchElementRef: ElementRef;

  form: FormGroup;

  sub: Subscription;

  categories:Category[] =[];

  min = new Date();
  max = new Date(2049,11,31);


  editorConfig = {
    editable: true,
    spellcheck: false,
    height: '10rem',
    minHeight: '5rem',
    placeholder: 'Event description...',
    translate: 'no',
    "toolbar": [
      ["bold", "italic", "underline", "strikeThrough"],
      ["fontSize", "color"],
      ["justifyLeft", "justifyCenter", "justifyRight", "justifyFull"],
      ["undo", "redo"],
      ["horizontalLine", "orderedList", "unorderedList"],
    ]
  };

  imageUploading = false;

  constructor(private auth: AuthService,
              private eventService: EventService,
              private formBuilder: FormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private mapsAPILoader: MapsAPILoader,
              private ngZone: NgZone) {

    this.uploader.onSuccessItem = (item: any, response: string, status: number, headers: any): any => {
      this.imageUploading = false;
      let res: any = JSON.parse(response);
      this.event.image = res.url;
      console.log(`res - ` + JSON.stringify(res) );
      return { item, response, status, headers };
    };
  }

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.eventService.getEvent(id).subscribe((event: any) => {
          if (event) {
            this.event = event;
            this.Position();
          } else {
            console.log(`Event with id '${id}' not found!`);
          }
        });
      }
    });
    this.form = this.formBuilder.group({
      eventNameControl: ['', [Validators.required]],
      descriptionControl: ['', [Validators.required]],
      timeLineStartControl: ['', [Validators.required]],
      timeLineFinishControl: ['', [Validators.required]],
      periodControl: ['', [Validators.required]],
        image: ['', ]},
      {validator: imageExtension('image')});
    this.getCategories();

    this.searchControl = new FormControl();

    this.mapsAPILoader.load().then(() => {
      const autocomplete = new google.maps.places.Autocomplete(this.searchElementRef.nativeElement, {
        types: ["address"]
      });
      autocomplete.addListener("place_changed", () => {
        this.ngZone.run(() => {

          const place: google.maps.places.PlaceResult = autocomplete.getPlace();

          if (place.geometry === undefined || place.geometry === null) {
            return;
          }
          this.latitude = place.geometry.location.lat();
          this.longitude = place.geometry.location.lng();
          this.event.place = this.latitude + "/" + this.longitude;
        });
      });
    });

  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  private Position() {

    let coords = this.event.place.split("/");
        this.latitude =Number(coords[0]);
        this.longitude =Number(coords[1]);
        console.log(this.latitude);
        console.log(this.longitude);

  }
  onChoseLocation(event) {
    this.latitude = event.coords.lat;
    this.longitude = event.coords.lng;
    this.event.place = this.latitude + "/" + this.longitude;
    console.log(this.event.place)
  }

  upload() {
    if (this.form.get("image").valid) {
      this.imageUploading = true;
      this.uploader.uploadAll();
    }
  }
  publish() {
    this.event.isSent = true;
    this.fixDate();
    this.eventService.updateEvent(this.event).subscribe((user: any) => {
      this.router.navigate(['event/', this.event.id]);
    }, error => console.error(error));
  }

  save() {
    this.event.isSent = false;
    this.fixDate();
    this.eventService.updateEvent(this.event).subscribe((user: any) => {
      this.router.navigate(['event', this.event.id]);
    }, error => console.error(error));
  }

  getCategories(){
    this.eventService.getCategories().subscribe((categories: any) => {
      this.categories = categories;
    });

  }

  fixDate(){
    this.event.timeLineStart = new Date(new Date(this.event.timeLineStart).getTime()
      + Math.abs(new Date(this.event.timeLineStart).getTimezoneOffset()*60000));
    this.event.timeLineFinish = new Date(new Date(this.event.timeLineFinish).getTime()
      + Math.abs(new Date(this.event.timeLineFinish).getTimezoneOffset()*60000));
  }

}
