import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BehaviorSubject, Observable, EMPTY, forkJoin } from 'rxjs';
import { catchError, switchMap, tap, shareReplay } from 'rxjs/operators';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth';
import { GraphqlService } from '../../../core/services/graphql.service';
import { 
  KpisGeneralesResponse, 
  DistribucionClustersResponse, 
  ReglaAsociacionEnriquecida,
  EvolucionClustersResponse,
  MapaRutasComplementariasResponse
} from '../../../core/models/business.models';

import { KpiCardsComponent } from '../components/kpi-cards.component';
import { ClusterDistributionChartComponent } from '../components/cluster-distribution-chart.component';
import { ClusterEvolutionChartComponent } from '../components/cluster-evolution-chart.component';
import { AssociationRulesTableComponent } from '../components/association-rules-table.component';
import { RouteHeatmapComponent } from '../components/route-heatmap.component';

@Component({
  selector: 'app-dashboard-bi',
  standalone: true,
  imports: [
    CommonModule, 
    KpiCardsComponent, 
    ClusterDistributionChartComponent,
    ClusterEvolutionChartComponent,
    AssociationRulesTableComponent,
    RouteHeatmapComponent
  ],
  templateUrl: './dashboard-bi.html',
  styleUrls: ['./dashboard-bi.css']
})
export class DashboardBi implements OnInit {
  private authService = inject(AuthService);
  private graphql = inject(GraphqlService);
  private router = inject(Router);

  // Observables para el template
  kpis$!: Observable<KpisGeneralesResponse>;
  distribucion$!: Observable<DistribucionClustersResponse>;
  evolucion$!: Observable<EvolucionClustersResponse>;
  mapa$!: Observable<MapaRutasComplementariasResponse>;
  
  ordenReglasSubject = new BehaviorSubject<string>('lift');
  reglas$!: Observable<ReglaAsociacionEnriquecida[]>;
  
  // State Signals
  activeTab = signal<string>('general');
  error = signal<string | null>(null);

  // Filtros globales/locales por defecto
  fechaInicioDefault = '';
  fechaFinDefault = '';

  ngOnInit() {
    this.initDefaultDates();
    this.cargarDashboard();
  }

  setActiveTab(tab: string) {
    this.activeTab.set(tab);
  }

  private initDefaultDates() {
    const hoy = new Date();
    const haceUnMes = new Date();
    haceUnMes.setMonth(hoy.getMonth() - 1);
    
    const pad = (n: number) => n.toString().padStart(2, '0');
    this.fechaFinDefault = `${hoy.getFullYear()}-${pad(hoy.getMonth() + 1)}-${pad(hoy.getDate())}`;
    this.fechaInicioDefault = `${haceUnMes.getFullYear()}-${pad(haceUnMes.getMonth() + 1)}-${pad(haceUnMes.getDate())}`;
  }

  cargarDashboard() {
    this.error.set(null);

    this.kpis$ = this.graphql.obtenerKpisGenerales().pipe(
      shareReplay(1),
      catchError(this.handleError.bind(this))
    );

    this.distribucion$ = this.graphql.obtenerDistribucionClusters().pipe(
      shareReplay(1),
      catchError(this.handleError.bind(this))
    );

    this.mapa$ = this.graphql.obtenerMapaRutasComplementarias().pipe(
      shareReplay(1),
      catchError(this.handleError.bind(this))
    );
    
    this.evolucion$ = this.graphql.obtenerEvolucionClusters(this.fechaInicioDefault, this.fechaFinDefault, 'MENSUAL').pipe(
      shareReplay(1),
      catchError(this.handleError.bind(this))
    );

    this.reglas$ = this.ordenReglasSubject.pipe(
      switchMap(orden => this.graphql.obtenerReglasAsociacion(20, orden).pipe(
        catchError(this.handleError.bind(this))
      )),
      shareReplay(1)
    );
  }

  cambiarOrdenReglas(orden: string) {
    this.ordenReglasSubject.next(orden);
  }

  actualizarFiltrosEvolucion(filtros: {inicio: string, fin: string, intervalo: string}) {
    this.evolucion$ = this.graphql.obtenerEvolucionClusters(filtros.inicio, filtros.fin, filtros.intervalo).pipe(
      shareReplay(1),
      catchError(this.handleError.bind(this))
    );
  }

  refrescarManual() {
    this.cargarDashboard();
  }

  private handleError(err: any) {
    this.error.set('Error al cargar datos del dashboard: ' + (err.message || 'Error desconocido'));
    return EMPTY;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
