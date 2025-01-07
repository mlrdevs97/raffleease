import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleCreationComponent } from './raffle-creation.component';

describe('RaffleCreationComponent', () => {
  let component: RaffleCreationComponent;
  let fixture: ComponentFixture<RaffleCreationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleCreationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
