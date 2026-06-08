import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KpisGeneralesResponse } from '../../../core/models/business.models';

@Component({
  selector: 'app-kpi-cards',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8" *ngIf="kpis">
      <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6 flex flex-col justify-between hover:shadow-lg transition-shadow">
        <div>
          <p class="text-sm font-medium text-gray-500 uppercase tracking-wide">Usuarios Segmentados</p>
          <div class="mt-2 flex items-baseline gap-2">
            <span class="text-3xl font-bold text-gray-900">{{ kpis.segmentacion.totalSegmentados }}</span>
            <span class="text-sm text-gray-500">de {{ kpis.segmentacion.totalUsuarios }} totales</span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6 flex flex-col justify-between hover:shadow-lg transition-shadow">
        <div>
          <p class="text-sm font-medium text-gray-500 uppercase tracking-wide">Reglas Encontradas</p>
          <div class="mt-2 flex items-baseline gap-2">
            <span class="text-3xl font-bold text-gray-900">{{ kpis.reglasAsociacion.totalReglas }}</span>
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
              {{ kpis.reglasAsociacion.reglasAltoLift }} alto lift
            </span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6 flex flex-col justify-between hover:shadow-lg transition-shadow">
        <div>
          <p class="text-sm font-medium text-gray-500 uppercase tracking-wide">Soporte Promedio</p>
          <div class="mt-2 flex items-baseline gap-2">
            <span class="text-3xl font-bold text-gray-900">{{ (kpis.reglasAsociacion.supportPromedioTop20 * 100).toFixed(2) }}%</span>
            <span class="text-sm text-gray-500">Top 20</span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6 flex flex-col justify-between hover:shadow-lg transition-shadow">
        <div>
          <p class="text-sm font-medium text-gray-500 uppercase tracking-wide">Índice Cross-Selling</p>
          <div class="mt-2 flex items-baseline gap-2">
            <span class="text-3xl font-bold text-blue-600">{{ kpis.reglasAsociacion.indiceCrossSelling }}</span>
            <span class="text-sm text-gray-500">Puntos</span>
          </div>
        </div>
      </div>
    </div>
  `
})
export class KpiCardsComponent {
  @Input() kpis!: KpisGeneralesResponse | null;
}
