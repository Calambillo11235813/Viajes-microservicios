import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardBi } from './dashboard-bi';

describe('DashboardBi', () => {
  let component: DashboardBi;
  let fixture: ComponentFixture<DashboardBi>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardBi]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardBi);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
