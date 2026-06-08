import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { DashboardBi } from './dashboard-bi';
import { GraphqlService } from '../../../core/services/graphql.service';
import { AuthService } from '../../../core/services/auth';

describe('DashboardBi', () => {
  let component: DashboardBi;
  let fixture: ComponentFixture<DashboardBi>;
  let mockGraphqlService: jasmine.SpyObj<GraphqlService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    mockGraphqlService = jasmine.createSpyObj('GraphqlService', [
      'obtenerKpisGenerales',
      'obtenerDistribucionClusters',
      'obtenerMapaRutasComplementarias',
      'obtenerEvolucionClusters',
      'obtenerReglasAsociacion'
    ]);
    
    mockAuthService = jasmine.createSpyObj('AuthService', ['logout']);

    // Setup mocks
    mockGraphqlService.obtenerKpisGenerales.and.returnValue(of({} as any));
    mockGraphqlService.obtenerDistribucionClusters.and.returnValue(of({} as any));
    mockGraphqlService.obtenerMapaRutasComplementarias.and.returnValue(of({} as any));
    mockGraphqlService.obtenerEvolucionClusters.and.returnValue(of({} as any));
    mockGraphqlService.obtenerReglasAsociacion.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DashboardBi, HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: GraphqlService, useValue: mockGraphqlService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardBi);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debe actualizar el BehaviorSubject al cambiar orden de reglas', () => {
    const spy = spyOn(component.ordenReglasSubject, 'next');
    component.cambiarOrdenReglas('confidence');
    expect(spy).toHaveBeenCalledWith('confidence');
  });

  it('debe iniciar la carga de los servicios al invocar cargarDashboard', () => {
    component.cargarDashboard();
    expect(mockGraphqlService.obtenerKpisGenerales).toHaveBeenCalled();
    expect(mockGraphqlService.obtenerDistribucionClusters).toHaveBeenCalled();
    expect(mockGraphqlService.obtenerMapaRutasComplementarias).toHaveBeenCalled();
  });
});
