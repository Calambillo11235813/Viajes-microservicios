import { afterNextRender, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { GraphqlService } from '../../../core/services/graphql.service';
import { RutaDestino } from '../../../core/models/business.models';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-rutas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './rutas.html'
})
export class Rutas {
  private fb = inject(FormBuilder);
  private graphqlService = inject(GraphqlService);
  public authService = inject(AuthService);

  rutas = signal<RutaDestino[]>([]);
  isLoading = signal(true);
  errorCarga = signal<string | null>(null);
  showModal = signal(false);

  rutaForm: FormGroup = this.fb.group({
    ciudadOrigen: ['', Validators.required],
    ciudadDestino: ['', Validators.required],
    duracionEstimadaHoras: [1, [Validators.required, Validators.min(0.5)]],
    precioBase: [50, [Validators.required, Validators.min(10)]],
    categoriaTuristica: ['Normal']
  });

  constructor() {
    afterNextRender(() => this.cargarRutas());
  }

  cargarRutas() {
    this.isLoading.set(true);
    this.errorCarga.set(null);

    this.graphqlService.listarRutas(0, 100).pipe(
      finalize(() => this.isLoading.set(false))
    ).subscribe({
      next: (data) => {
        this.rutas.set(data?.contenido ?? []);
      },
      error: (err) => {
        console.error('Error cargando rutas', err);
        this.rutas.set([]);
        this.errorCarga.set(err.message || 'No se pudieron cargar las rutas.');
      }
    });
  }

  abrirModal() {
    this.rutaForm.reset({ duracionEstimadaHoras: 1, precioBase: 50, categoriaTuristica: 'Normal' });
    this.showModal.set(true);
  }

  cerrarModal() {
    this.showModal.set(false);
  }

  guardarRuta() {
    if (this.rutaForm.invalid) return;

    this.graphqlService.crearRuta(this.rutaForm.value).subscribe({
      next: (nuevaRuta) => {
        this.rutas.update(lista => [...lista, nuevaRuta]);
        this.cerrarModal();
      },
      error: (err) => alert('Error al guardar la ruta: ' + err.message)
    });
  }

  eliminar(id: number | string) {
    if (confirm('¿Estás seguro de eliminar esta ruta?')) {
      this.graphqlService.eliminarRuta(Number(id)).subscribe({
        next: () => {
          this.rutas.update(lista => lista.filter(r => String(r.id) !== String(id)));
        },
        error: (err) => alert('Error al eliminar: ' + err.message)
      });
    }
  }
}
