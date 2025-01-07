import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleManagementComponent } from './raffle-management.component';

describe('RaffleManagementComponent', () => {
  let component: RaffleManagementComponent;
  let fixture: ComponentFixture<RaffleManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
