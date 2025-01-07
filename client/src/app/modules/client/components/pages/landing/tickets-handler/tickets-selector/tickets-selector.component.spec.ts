import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketsSelectorComponent } from './tickets-selector.component';

describe('TicketsSelectorComponent', () => {
  let component: TicketsSelectorComponent;
  let fixture: ComponentFixture<TicketsSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketsSelectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketsSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
