import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UploadImagesComponent } from '../../../../shared/upload-images/upload-images.component';
import { Router } from '@angular/router';
import { Image } from '../../../../../../../core/models/images/image';
import { correctDateValidator } from '../../../../../../../core/validators/correct-date.validator';
import { TicketsCreate } from '../../../../../../../core/models/tickets/raffle-tickets-creation-request';
import { RaffleCreate } from '../../../../../../../core/models/raffles/raffle-create';

@Component({
  selector: 'app-creation-form',
  standalone: true,
  imports: [ReactiveFormsModule, UploadImagesComponent],
  templateUrl: './creation-form.component.html',
  styleUrls: ['./creation-form.component.css']
})
export class CreationFormComponent {
  @Input() validationErrors: Record<string, string> = {};
  @Input() serverError: string | null = null;
  @Output() createRaffle = new EventEmitter<RaffleCreate>();
  raffleForm!: FormGroup;
  formSubmitted = false;

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) { }

  ngOnInit() {
    this.initializeForm();
  }

  initializeForm() {
    this.raffleForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      endDate: ['', [Validators.required, correctDateValidator]],
      images: this.fb.array([]),
      deleteImageIds: this.fb.array([]),
      imagesOrder: this.fb.array([]),
      amount: ['', [
        Validators.min(0),
        Validators.required
      ]],
      price: ['', Validators.required],
      lowerLimit: ['', [
        Validators.min(0),
        Validators.required
      ]],
    });
  }

  get images(): FormArray {
    return this.raffleForm.get('images') as FormArray;
  }

  get deleteImageIds(): FormArray {
    return this.raffleForm.get('deleteImageIds') as FormArray;
  }

  get imagesOrder(): FormArray {
    return this.raffleForm.get('imagesOrder') as FormArray;
  }

  setImages(images: Image[]) {
    this.images.clear();
    console.log(images);
    images.forEach(image => {
      this.images.push(this.fb.control(image));
      if (!this.images.dirty) this.images.markAsDirty();
    });
  }

  onDelete(id: number): void {
    this.deleteImageIds.push(this.fb.control(id));
  }

  getErrorMessage(field: string): string {
    if (this.validationErrors[field]) return this.validationErrors[field];

    const control: AbstractControl | null = this.raffleForm.get(field);

    if (!control) throw new Error("Control not found");

    if (control?.hasError('required')) return 'Este campo es obligatorio.';
    if (control?.hasError('min')) return 'El valor ingresado es menor al permitido.';
    if (control?.hasError('max')) return 'El valor ingresado es mayor al permitido.';
    if (control?.hasError('notFutureDate')) return 'La fecha debe ser en el futuro.';
    if (control?.hasError('exceedsOneYear')) return 'La fecha de finalización no puede ser superior a un año';

    return 'Unexpected error';
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.formSubmitted = true;

    if (this.raffleForm.invalid) return;

    const { title, description, endDate, images, amount, price, lowerLimit } = this.raffleForm.value;

    const ticketsInfo: TicketsCreate = {
      amount,
      price,
      lowerLimit
    };

    const raffleCreate: RaffleCreate = {
      title,
      description,
      endDate: new Date(`${endDate}T00:00:00`),
      images,
      ticketsInfo
    };

    this.createRaffle.emit(raffleCreate);
  }

  onCancel() {
    this.router.navigate([`/admin/panel`]);
  }
}
