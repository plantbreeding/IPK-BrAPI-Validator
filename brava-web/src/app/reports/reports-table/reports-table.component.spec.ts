import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportsTableComponent } from './reports-table.component';
import { SharedModule } from 'src/app/shared/shared.module';

describe('ReportsComponent', () => {
  let component: ReportsTableComponent;
  let fixture: ComponentFixture<ReportsComponent>;
  ReportsTableComponent
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ReportsTableComponent ],
      imports: [
        SharedModule,
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportsTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
