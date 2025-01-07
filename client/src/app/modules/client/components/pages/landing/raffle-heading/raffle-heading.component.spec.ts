import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleHeadingComponent } from './raffle-heading.component';

describe('RaffleHeadingComponent', () => {
  let component: RaffleHeadingComponent;
  let fixture: ComponentFixture<RaffleHeadingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleHeadingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleHeadingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
