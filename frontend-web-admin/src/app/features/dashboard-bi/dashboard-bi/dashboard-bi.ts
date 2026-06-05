import { Component, inject, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { GraphqlService } from '../../../core/services/graphql.service';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-dashboard-bi',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-bi.html'
})
export class DashboardBi implements OnInit, AfterViewInit {
  private authService = inject(AuthService);
  private graphqlService = inject(GraphqlService);
  private router = inject(Router);

  @ViewChild('chartIngresos') chartIngresos!: ElementRef;
  chartInstance: any;

  montoTotalMes = 0;
  cantidadPagosMes = 0;
  isLoading = true;

  ngOnInit() {
    this.cargarDatos();
  }

  ngAfterViewInit() {
    // Canvas ready
  }

  cargarDatos() {
    // Rango: Todo el mes actual
    const hoy = new Date();
    const inicio = new Date(hoy.getFullYear(), hoy.getMonth(), 1).toISOString();
    const fin = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0, 23, 59, 59).toISOString();

    this.graphqlService.generarReporteVentas(inicio, fin).subscribe({
      next: (reporte) => {
        this.montoTotalMes = reporte.montoTotal;
        this.cantidadPagosMes = reporte.cantidadPagos;
        this.isLoading = false;
        
        setTimeout(() => this.renderChart(reporte.detallesPorFecha), 100);
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  renderChart(detalles: any[]) {
    if(this.chartInstance) {
      this.chartInstance.destroy();
    }

    const labels = detalles.map(d => new Date(d.fecha).toLocaleDateString());
    const data = detalles.map(d => d.montoDia);

    const ctx = this.chartIngresos.nativeElement.getContext('2d');
    this.chartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: 'Ingresos (Bs)',
          data: data,
          borderColor: '#4f46e5',
          backgroundColor: 'rgba(79, 70, 229, 0.1)',
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: { beginAtZero: true }
        }
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
