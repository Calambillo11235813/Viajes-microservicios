import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Rutas } from './rutas';
import { GraphqlService } from '../../../core/services/graphql.service';
import { AuthService } from '../../../core/services/auth';

describe('Rutas', () => {
  let component: Rutas;
  let fixture: ComponentFixture<Rutas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Rutas],
      providers: [
        {
          provide: GraphqlService,
          useValue: {
            listarRutas: () => of({ contenido: [], totalPaginas: 0, totalElementos: 0, paginaActual: 0, tieneSiguiente: false })
          }
        },
        {
          provide: AuthService,
          useValue: { hasRole: () => true }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Rutas);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
