import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleCardComponent } from './raffle-card.component';

describe('RaffleCardComponent', () => {
  let component: RaffleCardComponent;
  let fixture: ComponentFixture<RaffleCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
