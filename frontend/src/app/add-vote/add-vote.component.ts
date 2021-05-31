import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserService} from '../_services/user.service';

@Component({
  selector: 'app-add-vote',
  templateUrl: './add-vote.component.html',
  styleUrls: ['./add-vote.component.css']
})
export class AddVoteComponent implements OnInit {
  @ViewChild('test', {static: false}) test!: ElementRef;
  maxLength = 10;
  minLength = 5;
  duration_options = ['30 seconds', '1 minute', '2 minutes', '3 minutes', '4 minutes', '5 minutes'];
  duration = 'Duration';
  subject = '';
  subjectError = false;

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.addInputMaxLength();
  }

  addInputMaxLength(): void {
    document.addEventListener('keydown', (evt) => {
      const subject = document.getElementById('subject');
      if ((evt.target === subject) && (this.subject.length === this.maxLength)) {
        this.subjectError = true;
        this.userService.sleep(3).subscribe(
          ans => {
            this.subjectError = false;
          });
      }
    });
  }

  selectDuration(option: string) {
    this.duration = option;
    this.test.nativeElement.click();
    this.test.nativeElement.style.cssText += 'color: #556B2F;';
  }

}
