import { Component, Input, ViewChild, ElementRef, OnChanges, SimpleChanges, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DistribucionClustersResponse } from '../../../core/models/business.models';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-cluster-distribution-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6">
      <h3 class="text-lg font-semibold text-gray-800 mb-4">Distribución por Segmentos</h3>
      <div class="relative h-64 w-full">
        <canvas #chartCanvas></canvas>
      </div>
    </div>
  `
})
export class ClusterDistributionChartComponent implements OnChanges, AfterViewInit {
  @Input() distribucion!: DistribucionClustersResponse | null;
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  private chart: Chart | null = null;

  ngAfterViewInit() {
    this.renderChart();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['distribucion'] && !changes['distribucion'].firstChange) {
      this.renderChart();
    }
  }

  private renderChart() {
    if (!this.distribucion || !this.chartCanvas) return;

    if (this.chart) {
      this.chart.destroy();
    }

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const labels = this.distribucion.clusters.map(c => c.etiqueta);
    const data = this.distribucion.clusters.map(c => c.porcentaje);

    this.chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: '% de Usuarios',
          data: data,
          backgroundColor: [
            'rgba(37, 99, 235, 0.8)', // Primary blue
            'rgba(20, 184, 166, 0.8)', // Secondary teal
            'rgba(234, 179, 8, 0.8)'   // Warning yellow
          ],
          borderRadius: 6
        }]
      },
      options: {
        indexAxis: 'y', // Barras horizontales
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          x: {
            beginAtZero: true,
            max: 100,
            title: { display: true, text: 'Porcentaje (%)' }
          }
        }
      }
    });
  }
}
