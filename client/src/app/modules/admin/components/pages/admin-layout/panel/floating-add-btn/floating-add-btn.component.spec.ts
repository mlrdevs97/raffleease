import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FloatingAddBtnComponent } from './floating-add-btn.component';

describe('FloatingAddBtnComponent', () => {
  let component: FloatingAddBtnComponent;
  let fixture: ComponentFixture<FloatingAddBtnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FloatingAddBtnComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FloatingAddBtnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
