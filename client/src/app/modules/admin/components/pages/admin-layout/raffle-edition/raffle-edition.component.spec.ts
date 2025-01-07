import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleEditionComponent } from './raffle-edition.component';

describe('RaffleEditionComponent', () => {
  let component: RaffleEditionComponent;
  let fixture: ComponentFixture<RaffleEditionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleEditionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleEditionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
