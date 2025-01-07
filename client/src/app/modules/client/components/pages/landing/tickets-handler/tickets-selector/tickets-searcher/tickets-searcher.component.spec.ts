import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketsSearcherComponent } from './tickets-searcher.component';

describe('TicketsSearcherComponent', () => {
  let component: TicketsSearcherComponent;
  let fixture: ComponentFixture<TicketsSearcherComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketsSearcherComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketsSearcherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
