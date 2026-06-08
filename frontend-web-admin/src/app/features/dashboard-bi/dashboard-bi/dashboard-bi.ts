import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { GraphqlService } from '../../../core/services/graphql.service';
import { NgxChartsModule, ScaleType } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-dashboard-bi',
  standalone: true,
  imports: [CommonModule, NgxChartsModule],
  templateUrl: './dashboard-bi.html',
  styleUrls: ['./dashboard-bi.css']
})
export class DashboardBi implements OnInit {
  private authService = inject(AuthService);
  private graphqlService = inject(GraphqlService);
  private router = inject(Router);

  montoTotalMes = 0;
  cantidadPagosMes = 0;
  ocupacionFlota = 0;
  isLoading = true;

  // ngx-charts configuration
  chartData: any[] = [];
  view: [number, number] = [700, 400];
  showXAxis = true;
  showYAxis = true;
  gradient = false;
  showLegend = false;
  showXAxisLabel = true;
  xAxisLabel = 'Fecha';
  showYAxisLabel = true;
  yAxisLabel = 'Ingresos (Bs)';
  
  // Custom scheme
  colorScheme: any = {
    name: 'custom',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#4f46e5', '#3b82f6', '#10b981', '#f59e0b', '#ef4444']
  };

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    // Rango: Todo el mes actual
    const hoy = new Date();
    const pad = (n: number) => n.toString().padStart(2, '0');
    const inicio = `${hoy.getFullYear()}-${pad(hoy.getMonth() + 1)}-01`;
    const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0).getDate();
    const fin = `${hoy.getFullYear()}-${pad(hoy.getMonth() + 1)}-${pad(ultimoDia)}`;

    this.graphqlService.generarReporteVentas(inicio, fin).subscribe({
      next: (reporte) => {
        this.montoTotalMes = reporte.montoTotal;
        this.cantidadPagosMes = reporte.cantidadPagos;
        this.ocupacionFlota = reporte.ocupacionFlota;
        
        // Map data for ngx-charts
        this.chartData = [
          {
            name: 'Ingresos Diarios',
            series: reporte.detallesPorFecha.map(d => ({
              name: new Date(d.fecha).toLocaleDateString(),
              value: d.montoDia
            }))
          }
        ];
        
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
