import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-add-vote',
  templateUrl: './add-vote.component.html',
  styleUrls: ['./add-vote.component.css']
})
export class AddVoteComponent implements OnInit {
  @ViewChild('test', {static: false}) test!: ElementRef;
  duration_options = ['30 seconds', '1 minute', '2 minutes', '3 minutes', '4 minutes', '5 minutes'];
  duration = 'Duration';

  constructor() { }

  ngOnInit(): void {
  }

  selectDuration(option: string) {
    this.duration = option;
    this.test.nativeElement.click();
    this.test.nativeElement.style.cssText += 'color: #556B2F;';
  }

}
