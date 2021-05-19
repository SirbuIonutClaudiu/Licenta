import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-email-successfull',
  templateUrl: './email-successfull.component.html',
  styleUrls: ['./email-successfull.component.css']
})
export class EmailSuccessfullComponent implements OnInit {

  public answer: boolean = false;

  constructor(private _Activatedroute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.answer= (this._Activatedroute.snapshot.paramMap.get("answer") == "successfull");
  }
}
