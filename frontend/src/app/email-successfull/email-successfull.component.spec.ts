import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmailSuccessfullComponent } from './email-successfull.component';

describe('EmailSuccessfullComponent', () => {
  let component: EmailSuccessfullComponent;
  let fixture: ComponentFixture<EmailSuccessfullComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmailSuccessfullComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EmailSuccessfullComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
