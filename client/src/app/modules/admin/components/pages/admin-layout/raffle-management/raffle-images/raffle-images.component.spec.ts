import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaffleImagesComponent } from './raffle-images.component';

describe('RaffleImagesComponent', () => {
  let component: RaffleImagesComponent;
  let fixture: ComponentFixture<RaffleImagesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RaffleImagesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaffleImagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
