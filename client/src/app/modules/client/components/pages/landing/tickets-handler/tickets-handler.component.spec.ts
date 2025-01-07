import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketsHandlerComponent } from './tickets-handler.component';

describe('TicketsHandlerComponent', () => {
  let component: TicketsHandlerComponent;
  let fixture: ComponentFixture<TicketsHandlerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketsHandlerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketsHandlerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
