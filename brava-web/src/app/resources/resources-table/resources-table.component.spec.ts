import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourcesTableComponent } from './resources-table.component';

describe('ResourcesTableComponent', () => {
  let component: ResourcesTableComponent;
  let fixture: ComponentFixture<ResourcesTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResourcesTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResourcesTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
