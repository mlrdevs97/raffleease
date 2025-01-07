import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseBtnComponent } from './purchase-btn.component';

describe('PurchaseBtnComponent', () => {
  let component: PurchaseBtnComponent;
  let fixture: ComponentFixture<PurchaseBtnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PurchaseBtnComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PurchaseBtnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
