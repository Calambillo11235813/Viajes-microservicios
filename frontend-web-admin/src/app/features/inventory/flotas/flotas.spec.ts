import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Flotas } from './flotas';

describe('Flotas', () => {
  let component: Flotas;
  let fixture: ComponentFixture<Flotas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Flotas],
    }).compileComponents();

    fixture = TestBed.createComponent(Flotas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
