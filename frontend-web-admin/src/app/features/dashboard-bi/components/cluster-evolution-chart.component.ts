import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, OnChanges, SimpleChanges, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EvolucionClustersResponse } from '../../../core/models/business.models';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-cluster-evolution-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-4 gap-4">
        <h3 class="text-lg font-semibold text-gray-800">Evolución de Clusters</h3>
        
        <div class="flex items-center gap-3 bg-gray-50 p-2 rounded-lg border border-gray-200">
          <input type="date" class="bg-white border border-gray-300 rounded px-2 py-1 text-sm focus:ring-blue-500 focus:border-blue-500"
                 [value]="fechaInicio" (change)="onFechaInicioChange($event)">
          <span class="text-gray-500 text-sm">a</span>
          <input type="date" class="bg-white border border-gray-300 rounded px-2 py-1 text-sm focus:ring-blue-500 focus:border-blue-500"
                 [value]="fechaFin" (change)="onFechaFinChange($event)">
          
          <select class="bg-white border border-gray-300 rounded px-2 py-1 text-sm focus:ring-blue-500 focus:border-blue-500"
                  [value]="intervalo" (change)="onIntervaloChange($event)">
            <option value="DIARIO">Diario</option>
            <option value="SEMANAL">Semanal</option>
            <option value="MENSUAL">Mensual</option>
          </select>
          
          <button class="bg-blue-600 hover:bg-blue-700 text-white p-1.5 rounded-md transition-colors" (click)="aplicarFiltros()">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
      </div>
      
      <div class="relative h-64 w-full">
        <canvas #chartCanvas></canvas>
      </div>
    </div>
  `
})
export class ClusterEvolutionChartComponent implements OnChanges, AfterViewInit {
  @Input() evolucion!: EvolucionClustersResponse | null;
  
  @Output() filtrosCambiados = new EventEmitter<{inicio: string, fin: string, intervalo: string}>();

  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  // Valores locales temporales hasta que se apliquen
  fechaInicio = '';
  fechaFin = '';
  intervalo = 'MENSUAL';

  private chart: Chart | null = null;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['evolucion'] && this.evolucion) {
      // Sincronizar inputs si cambian externamente
      if (!this.fechaInicio) this.fechaInicio = this.evolucion.fechaInicio;
      if (!this.fechaFin) this.fechaFin = this.evolucion.fechaFin;
      if (!this.intervalo) this.intervalo = this.evolucion.intervalo;
      
      this.renderChart();
    }
  }

  ngAfterViewInit() {
    this.renderChart();
  }

  onFechaInicioChange(event: any) { this.fechaInicio = event.target.value; }
  onFechaFinChange(event: any) { this.fechaFin = event.target.value; }
  onIntervaloChange(event: any) { this.intervalo = event.target.value; }

  aplicarFiltros() {
    this.filtrosCambiados.emit({
      inicio: this.fechaInicio,
      fin: this.fechaFin,
      intervalo: this.intervalo
    });
  }

  private renderChart() {
    if (!this.evolucion || !this.chartCanvas) return;

    if (this.chart) {
      this.chart.destroy();
    }

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const labels = this.evolucion.serie.map(s => s.fecha);
    
    // Obtener todos los IDs de cluster únicos en la serie temporal
    const clusterIds = new Set<number>();
    this.evolucion.serie.forEach(s => s.clusters.forEach(c => clusterIds.add(c.clusterId)));

    const colors = [
      'rgba(37, 99, 235, 1)',   // Blue
      'rgba(20, 184, 166, 1)',  // Teal
      'rgba(234, 179, 8, 1)',   // Yellow
      'rgba(168, 85, 247, 1)'   // Purple
    ];

    const datasets = Array.from(clusterIds).map((cId, i) => {
      const data = this.evolucion!.serie.map(s => {
        const found = s.clusters.find(c => c.clusterId === cId);
        return found ? found.cantidad : 0;
      });

      return {
        label: `Cluster ${cId}`,
        data: data,
        borderColor: colors[i % colors.length],
        backgroundColor: colors[i % colors.length],
        tension: 0.3,
        fill: false
      };
    });

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: datasets
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' }
        },
        scales: {
          y: {
            beginAtZero: true,
            title: { display: true, text: 'Cantidad Usuarios' }
          }
        }
      }
    });
  }
}
