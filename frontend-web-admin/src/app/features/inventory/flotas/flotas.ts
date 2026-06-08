import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GraphqlService } from '../../../core/services/graphql.service';
import { Flota } from '../../../core/models/business.models';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-flotas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './flotas.html',
  styleUrl: './flotas.css',
})
export class Flotas implements OnInit {
  private fb = inject(FormBuilder);
  private graphql = inject(GraphqlService);
  public authService = inject(AuthService);

  flotas: Flota[] = [];
  flotaForm: FormGroup;
  showModal = false;
  isEditing = false;
  currentIdBus: number | null = null;
  isLoading = false;

  constructor() {
    this.flotaForm = this.fb.group({
      placa: ['', Validators.required],
      tipoBus: ['', Validators.required],
      capacidadTotalAsientos: [40, [Validators.required, Validators.min(10), Validators.max(100)]]
    });
  }

  ngOnInit() {
    this.cargarFlotas();
  }

  cargarFlotas() {
    this.isLoading = true;
    this.graphql.listarFlotas().subscribe({
      next: (data) => {
        this.flotas = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar flotas', err);
        this.isLoading = false;
      }
    });
  }

  openModal(flota?: Flota) {
    if (flota && flota.idBus) {
      this.isEditing = true;
      this.currentIdBus = flota.idBus;
      this.flotaForm.patchValue({
        placa: flota.placa,
        tipoBus: flota.tipoBus,
        capacidadTotalAsientos: flota.capacidadTotalAsientos
      });
    } else {
      this.isEditing = false;
      this.currentIdBus = null;
      this.flotaForm.reset({ capacidadTotalAsientos: 40 });
    }
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.flotaForm.reset();
  }

  onSubmit() {
    if (this.flotaForm.invalid) return;

    if (this.isEditing && this.currentIdBus) {
      this.graphql.actualizarFlota(this.currentIdBus, this.flotaForm.value).subscribe({
        next: () => {
          this.cargarFlotas();
          this.closeModal();
        },
        error: (err) => console.error(err)
      });
    } else {
      this.graphql.crearFlota(this.flotaForm.value).subscribe({
        next: () => {
          this.cargarFlotas();
          this.closeModal();
        },
        error: (err) => console.error(err)
      });
    }
  }

  eliminar(idBus: number) {
    if (confirm('¿Estás seguro de que deseas eliminar este bus?')) {
      this.graphql.eliminarFlota(idBus).subscribe({
        next: () => this.cargarFlotas(),
        error: (err) => console.error(err)
      });
    }
  }
}
