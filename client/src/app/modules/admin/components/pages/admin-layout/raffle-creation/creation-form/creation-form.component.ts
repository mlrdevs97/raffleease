import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Form, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RaffleCreationRequest } from '../../../../../../../core/models/raffles/raffle-creation-request';
import { UploadImagesComponent } from '../../../../shared/upload-images/upload-images.component';
import { RaffleTicketsCreationRequest } from '../../../../../../../core/models/tickets/raffle-tickets-creation-request';
import { futureDateValidator } from '../../../../../../../core/validators/futureDateValidator';
import { Router } from '@angular/router';

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
  @Output() createRaffle: EventEmitter<RaffleCreationRequest> = new EventEmitter<RaffleCreationRequest>();
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
      endDate: ['', [Validators.required, futureDateValidator]],
      images: this.fb.array([]),
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

  setImages(images: { id: number | null; file: File, url: string }[], markAsDirty = true) {
    this.images.clear();
    images.forEach(image => {
      console.log(image);
      this.images.push(this.fb.control(image.file));
      if (markAsDirty) this.images.markAsDirty();
    });
  }

  getErrorMessage(field: string): string {
    if (this.validationErrors[field]) return this.validationErrors[field];

    const control = this.raffleForm.get(field);

    if (control?.hasError('required')) return 'Este campo es obligatorio.';
    if (control?.hasError('min')) return 'El valor ingresado es menor al permitido.';
    if (control?.hasError('max')) return 'El valor ingresado es mayor al permitido.';
    if (control?.hasError('notFutureDate')) return 'La fecha debe ser en el futuro.';
    return '';
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.formSubmitted = true;

    if (this.raffleForm.invalid) return;

    const { title, description, endDate, images, amount, price, lowerLimit } = this.raffleForm.value;

    const ticketsInfo: RaffleTicketsCreationRequest = {
      amount,
      price,
      lowerLimit
    };

    const endDateTime: Date = new Date(`${endDate}T00:00:00`);

    const request: RaffleCreationRequest = {
      title,
      description,
      endDate: endDateTime,
      images,
      ticketsInfo
    };

    console.log(request);

    this.createRaffle.emit(request);
  }

  onCancel() {
    this.router.navigate([`/admin/panel`]);
  }
}
