import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GraphqlService } from '../../../core/services/graphql.service';
import { RutaDestino } from '../../../core/models/business.models';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-rutas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './rutas.html'
})
export class Rutas implements OnInit {
  private fb = inject(FormBuilder);
  private graphqlService = inject(GraphqlService);
  public authService = inject(AuthService);

  rutas: RutaDestino[] = [];
  isLoading = false;
  showModal = false;
  
  rutaForm: FormGroup = this.fb.group({
    ciudadOrigen: ['', Validators.required],
    ciudadDestino: ['', Validators.required],
    duracionEstimadaHoras: [1, [Validators.required, Validators.min(0.5)]],
    precioBase: [50, [Validators.required, Validators.min(10)]],
    categoriaTuristica: ['Normal']
  });

  ngOnInit(): void {
    this.cargarRutas();
  }

  cargarRutas() {
    this.isLoading = true;
    this.graphqlService.listarRutas(0, 100).subscribe({
      next: (data) => {
        this.rutas = data.contenido;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error cargando rutas', err);
        this.isLoading = false;
      }
    });
  }

  abrirModal() {
    this.rutaForm.reset({ duracionEstimadaHoras: 1, precioBase: 50, categoriaTuristica: 'Normal' });
    this.showModal = true;
  }

  cerrarModal() {
    this.showModal = false;
  }

  guardarRuta() {
    if (this.rutaForm.invalid) return;

    this.graphqlService.crearRuta(this.rutaForm.value).subscribe({
      next: (nuevaRuta) => {
        this.rutas.push(nuevaRuta);
        this.cerrarModal();
      },
      error: (err) => alert('Error al guardar la ruta: ' + err.message)
    });
  }

  eliminar(id: number) {
    if (confirm('¿Estás seguro de eliminar esta ruta?')) {
      this.graphqlService.eliminarRuta(id).subscribe({
        next: () => {
          this.rutas = this.rutas.filter(r => r.id !== id);
        },
        error: (err) => alert('Error al eliminar: ' + err.message)
      });
    }
  }
}
