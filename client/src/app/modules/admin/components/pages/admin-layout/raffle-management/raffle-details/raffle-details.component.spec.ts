import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleDetailsComponent } from './raffle-details.component';

describe('RaffleDetailsComponent', () => {
  let component: RaffleDetailsComponent;
  let fixture: ComponentFixture<RaffleDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
