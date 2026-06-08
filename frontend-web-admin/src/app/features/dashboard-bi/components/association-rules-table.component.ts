import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReglaAsociacionEnriquecida } from '../../../core/models/business.models';

@Component({
  selector: 'app-association-rules-table',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6 overflow-hidden flex flex-col h-full">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-4 gap-4">
        <h3 class="text-lg font-semibold text-gray-800">Oportunidades de Cross-Selling</h3>
        
        <div class="flex items-center gap-2">
          <label class="text-sm text-gray-600 font-medium">Ordenar por:</label>
          <select class="bg-gray-50 border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:ring-blue-500 focus:border-blue-500"
                  [value]="ordenActual" (change)="onOrdenChange($event)">
            <option value="lift">Lift (Relevancia)</option>
            <option value="confidence">Confianza (Probabilidad)</option>
            <option value="support">Soporte (Frecuencia)</option>
          </select>
        </div>
      </div>
      
      <div class="overflow-x-auto flex-1">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Regla (Antecedente → Consecuente)</th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Soporte</th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Confianza</th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Lift</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr *ngFor="let regla of reglas" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4">
                <div class="text-sm font-medium text-gray-900">{{ formatAntecedentes(regla.antecedentes) }}</div>
                <div class="text-sm text-blue-600 font-semibold flex items-center gap-1 mt-1">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                  </svg>
                  {{ regla.consecuente.descripcion }}
                </div>
                <div class="text-xs text-gray-500 mt-1 italic">{{ regla.interpretacion }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-500">
                {{ (regla.soporte * 100).toFixed(1) }}%
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-500">
                {{ (regla.confianza * 100).toFixed(1) }}%
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      [ngClass]="{'bg-green-100 text-green-800': regla.lift >= 1.2, 'bg-gray-100 text-gray-800': regla.lift < 1.2}">
                  {{ regla.lift.toFixed(2) }}x
                </span>
              </td>
            </tr>
            <tr *ngIf="!reglas || reglas.length === 0">
              <td colspan="4" class="px-6 py-8 text-center text-gray-500 text-sm">
                No hay reglas de asociación disponibles para mostrar.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AssociationRulesTableComponent {
  @Input() reglas: ReglaAsociacionEnriquecida[] | null = [];
  @Input() ordenActual: string = 'lift';
  
  @Output() ordenCambiado = new EventEmitter<string>();

  onOrdenChange(event: any) {
    this.ordenCambiado.emit(event.target.value);
  }

  formatAntecedentes(ants: { descripcion: string }[]): string {
    if (!ants || ants.length === 0) return '';
    return ants.map(a => a.descripcion).join(' y ');
  }
}
