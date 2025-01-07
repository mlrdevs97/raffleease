import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RandomSelectorComponent } from './random-selector.component';

describe('RandomSelectorComponent', () => {
  let component: RandomSelectorComponent;
  let fixture: ComponentFixture<RandomSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RandomSelectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RandomSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
