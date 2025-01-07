import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleDescriptionComponent } from './raffle-description.component';

describe('RaffleDescriptionComponent', () => {
  let component: RaffleDescriptionComponent;
  let fixture: ComponentFixture<RaffleDescriptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleDescriptionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
