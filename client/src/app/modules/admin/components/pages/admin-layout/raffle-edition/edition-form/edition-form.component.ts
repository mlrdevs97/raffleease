import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { UploadImagesComponent } from '../../../../shared/upload-images/upload-images.component';
import { Raffle } from '../../../../../../../core/models/raffles/raffle';
import { totalTicketsValidator } from '../../../../../../../core/validators/total-tickets.validator';
import { Router } from '@angular/router';
import { Image } from '../../../../../../../core/models/images/image';
import { correctDateValidator } from '../../../../../../../core/validators/correct-date.validator';
import { RaffleEdit } from '../../../../../../../core/models/raffles/raffle-edit';

@Component({
  selector: 'app-edition-form',
  standalone: true,
  imports: [ReactiveFormsModule, UploadImagesComponent],
  templateUrl: './edition-form.component.html',
  styleUrls: ['./edition-form.component.css']
})
export class EditionFormComponent {
  @Input() validationErrors: Record<string, string> = {};
  @Input() serverError: string | null = null;
  @Input() raffle!: Raffle;
  @Output() editRaffle = new EventEmitter<Partial<RaffleEdit>>();
  raffleForm!: FormGroup;
  formSubmitted: boolean = false;

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) { }

  initializeForm() {
    this.raffleForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      endDate: ['', [Validators.required, correctDateValidator]],
      images: this.fb.array([]),
      totalTickets: ['', [
        Validators.min(this.raffle.totalTickets),
        Validators.required
      ]],
      ticketPrice: ['', Validators.required],
      lowerLimit: ['', [
        Validators.min(this.raffle.firstTicketNumber),
        Validators.max(this.raffle.firstTicketNumber),
        Validators.required
      ]],
    }, { Validators: totalTicketsValidator });

    this.fillForm(this.raffle);
  }

  fillForm(raffle: Raffle): void {
    const endDateTime = new Date(raffle.endDate);
    this.raffleForm.patchValue({
      title: raffle.title,
      description: raffle.description,
      endDate: endDateTime.toISOString().split('T')[0],
      totalTickets: raffle.totalTickets,
      ticketPrice: raffle.ticketPrice,
      lowerLimit: raffle.firstTicketNumber
    });
  }

  get lastNumber(): number {
    return this.raffle.firstTicketNumber + this.raffle.totalTickets - 1;
  }

  get images(): FormArray {
    return this.raffleForm.get('images') as FormArray;
  }

  setImages(images: Image[]): void {
    this.images.clear();
    images.forEach((image: Image) => {
      this.images.push(this.fb.control(image));
    });
    if (!this.images.dirty) this.images.markAsDirty();
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.formSubmitted = true;

    if (this.raffleForm.invalid) return;

    const raffleEdit: Partial<RaffleEdit> = this.getModifiedFields();

    this.editRaffle.emit(raffleEdit);
  }

  getErrorMessage(field: string): string {
    if (this.validationErrors[field]) return this.validationErrors[field];

    const control: AbstractControl | null = this.raffleForm.get(field);

    if (!control) throw new Error();

    if (control?.hasError('required')) return 'Este campo es obligatorio.';
    if (control?.hasError('min')) return 'El valor ingresado es menor al permitido.';
    if (control?.hasError('max')) return 'El valor ingresado es mayor al permitido.';
    if (control?.hasError('notFutureDate')) return 'La fecha debe ser en el futuro.';
    if (control?.hasError('exceedsOneYear')) return 'La fecha de finalización no puede ser superior a un año';
    if (control?.hasError('wrongQuantity')) return `La cantidad de tickets no puede ser menor a la cantidad original: ${this.raffle.totalTickets}.`;

    return 'Unexpected error';
  }

  getModifiedFields(): Partial<RaffleEdit> {
    const modifiedData: Partial<RaffleEdit> = {};

    Object.keys(this.raffleForm.controls).forEach((key) => {
      const control = this.raffleForm.get(key);
      if (control?.dirty) {
        if (key === 'endDate') {
          const endDate: string = this.raffleForm.get('endDate')?.value;
          modifiedData.endDate = new Date(`${endDate}T00:00:00`);
        } else {
          modifiedData[key as keyof RaffleEdit] = control.value;
        }
      }
    });
    return modifiedData;
  }

  onCancel() {
    this.router.navigate([`/admin/management/${this.raffle.id}`]);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['raffle'] && changes['raffle'].currentValue) {
      this.initializeForm();
    }
  }
}
