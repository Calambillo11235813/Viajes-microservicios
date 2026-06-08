import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapaRutasComplementariasResponse } from '../../../core/models/business.models';

@Component({
  selector: 'app-route-heatmap',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-xl shadow-md border border-gray-100 p-6 overflow-x-auto">
      <h3 class="text-lg font-semibold text-gray-800 mb-4">Mapa de Rutas Complementarias (Matriz de Co-ocurrencia)</h3>
      
      <div *ngIf="mapa && rutasIds.length > 0; else noData" class="inline-block min-w-full">
        <!-- Header Row (Destinos) -->
        <div class="flex">
          <div class="w-32 flex-shrink-0"></div> <!-- Esquina vacía -->
          <div *ngFor="let id of rutasIds" class="w-12 h-32 flex-shrink-0 relative">
            <div class="absolute bottom-2 left-1/2 -translate-x-1/2 -rotate-45 origin-bottom-left text-xs text-gray-500 whitespace-nowrap overflow-hidden text-ellipsis max-w-xs">
              {{ getNombreRuta(id) | slice:0:20 }}...
            </div>
          </div>
        </div>
        
        <!-- Data Rows (Orígenes) -->
        <div *ngFor="let origenId of rutasIds" class="flex mb-1">
          <div class="w-32 flex-shrink-0 text-right pr-4 text-xs font-medium text-gray-600 truncate flex items-center justify-end" [title]="getNombreRuta(origenId)">
            {{ getNombreRuta(origenId) | slice:0:20 }}
          </div>
          
          <div *ngFor="let destinoId of rutasIds" class="w-12 h-12 flex-shrink-0 border border-white relative group">
            <!-- Cell content -->
            <div class="w-full h-full rounded flex items-center justify-center transition-colors"
                 [ngStyle]="{'background-color': getCellColor(origenId, destinoId)}">
              <span class="text-[10px] font-bold text-white opacity-0 group-hover:opacity-100 transition-opacity drop-shadow-md">
                {{ getLift(origenId, destinoId) ? getLift(origenId, destinoId) + 'x' : '' }}
              </span>
            </div>
            
            <!-- Tooltip -->
            <div class="absolute z-10 hidden group-hover:block bottom-full left-1/2 -translate-x-1/2 mb-2 w-48 bg-gray-900 text-white text-xs rounded p-2 shadow-lg pointer-events-none">
              <p class="font-semibold border-b border-gray-700 pb-1 mb-1">Co-ocurrencia</p>
              <p><span class="text-gray-400">Si compra:</span> {{ getNombreRuta(origenId) }}</p>
              <p><span class="text-gray-400">También:</span> {{ getNombreRuta(destinoId) }}</p>
              <p class="mt-1 text-blue-300 font-bold">Lift: {{ getLift(origenId, destinoId) || 0 }}x</p>
            </div>
          </div>
        </div>
        
        <!-- Leyenda -->
        <div class="mt-6 flex items-center justify-end gap-2 text-xs text-gray-500">
          <span>Menor Lift</span>
          <div class="w-24 h-3 rounded bg-gradient-to-r from-blue-100 to-blue-800"></div>
          <span>Mayor Lift</span>
        </div>
      </div>
      
      <ng-template #noData>
        <div class="p-8 text-center text-gray-500 text-sm">
          No hay datos de matriz de co-ocurrencia suficientes.
        </div>
      </ng-template>
    </div>
  `
})
export class RouteHeatmapComponent implements OnChanges {
  @Input() mapa!: MapaRutasComplementariasResponse | null;
  
  rutasIds: number[] = [];
  rutasMap: Map<number, string> = new Map();
  matrizMap: Map<string, number> = new Map(); // key: "origen-destino", value: lift
  
  maxLift = 1;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['mapa'] && this.mapa) {
      this.procesarDatos();
    }
  }

  private procesarDatos() {
    this.rutasIds = [];
    this.rutasMap.clear();
    this.matrizMap.clear();
    this.maxLift = 1;

    if (!this.mapa || !this.mapa.rutas || !this.mapa.matriz) return;

    this.mapa.rutas.forEach(r => {
      this.rutasIds.push(r.idRuta);
      this.rutasMap.set(r.idRuta, r.descripcion);
    });

    this.mapa.matriz.forEach(c => {
      const key = `${c.rutaOrigen}-${c.rutaDestino}`;
      this.matrizMap.set(key, c.lift);
      if (c.lift > this.maxLift) {
        this.maxLift = c.lift;
      }
    });
  }

  getNombreRuta(id: number): string {
    return this.rutasMap.get(id) || `Ruta ${id}`;
  }

  getLift(origen: number, destino: number): string {
    const lift = this.matrizMap.get(`${origen}-${destino}`);
    return lift ? lift.toFixed(1) : '';
  }

  getCellColor(origen: number, destino: number): string {
    if (origen === destino) return '#f3f4f6'; // gris claro para la diagonal
    
    const lift = this.matrizMap.get(`${origen}-${destino}`);
    if (!lift) return '#f9fafb'; // muy claro si no hay relación
    
    // Escala de azules: bg-blue-100 (min) a bg-blue-800 (max)
    // Para simplificar, calculamos la opacidad del azul en base al lift
    // lift 1 = opacidad baja, lift max = opacidad alta
    const minOpacity = 0.1;
    let opacity = minOpacity;
    
    if (lift >= 1) {
      const ratio = (lift - 1) / (this.maxLift - 1 || 1); // Evitar div 0
      opacity = minOpacity + (ratio * (1 - minOpacity));
    }
    
    // Color base blue-700 (rgb 29, 78, 216)
    return `rgba(29, 78, 216, ${Math.min(opacity, 1)})`;
  }
}
