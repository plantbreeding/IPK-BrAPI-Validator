import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValidationRequestComponent } from './validation-request.component';

describe('ValidationRequestComponent', () => {
  let component: ValidationRequestComponent;
  let fixture: ComponentFixture<ValidationRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ValidationRequestComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ValidationRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
