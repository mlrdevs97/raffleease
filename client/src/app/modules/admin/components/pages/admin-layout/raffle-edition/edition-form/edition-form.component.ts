import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { UploadImagesComponent } from '../../../../shared/upload-images/upload-images.component';
import { EditRaffle } from '../../../../../../../core/models/raffles/edit-raffle';
import { Raffle } from '../../../../../../../core/models/raffles/raffle';
import { futureDateValidator } from '../../../../../../../core/validators/futureDateValidator';
import { totalTicketsValidator } from '../../../../../../../core/validators/totalTicetsValidator';
import { Router } from '@angular/router';
import { ImageFile } from '../../../../../../../core/models/raffles/images/image-file';

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
  @Output() editRaffle: EventEmitter<EditRaffle> = new EventEmitter<EditRaffle>();
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
      endDate: ['', [Validators.required, futureDateValidator]],
      newImages: this.fb.array([]),
      deleteImageIds: this.fb.array([]),
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

  createImageUrl(imageFile: ImageFile): string {
    const blob = new Blob([imageFile.data], { type: imageFile.contentType });
    return URL.createObjectURL(blob);
  }

  get lastNumber(): number {
    return this.raffle.firstTicketNumber + this.raffle.totalTickets - 1;
  }

  get deleteImageIds(): FormArray {
    return this.raffleForm.get('deleteImageIds') as FormArray;
  }
  
  get newImages(): FormArray {
    return this.raffleForm.get('newImages') as FormArray;
  }

  setImages(files: { id: number | null; file: File, url: string }[]) {
    this.deleteImageIds.clear();
    this.newImages.clear();
    files.forEach(file => {
      if (!file.id) this.deleteImageIds.push(this.fb.control(file.id));
      this.newImages.push(this.fb.control(file));
    });
  }
  
  onSubmit(event: Event) {
    event.preventDefault();
    this.formSubmitted = true;

    if (this.raffleForm.invalid) return;

    const modifiedData: Partial<EditRaffle> = this.getModifiedFields();
    this.editRaffle.emit(modifiedData);
  }

  getErrorMessage(field: string): string {
    if (this.validationErrors[field]) return this.validationErrors[field];

    const control = this.raffleForm.get(field);

    if (control?.hasError('required')) return 'Este campo es obligatorio.';
    if (control?.hasError('min')) return 'El valor ingresado es menor al permitido.';
    if (control?.hasError('max')) return 'El valor ingresado es mayor al permitido.';
    if (control?.hasError('notFutureDate')) return 'La fecha debe ser en el futuro.';
    if (control?.hasError('wrongQuantity')) return `La cantidad de tickets no puede ser menor a la cantidad original: ${this.raffle.totalTickets}.`;
    return '';
  }

  getModifiedFields(): Partial<EditRaffle> {
    const modifiedData: Partial<EditRaffle> = {};
    Object.keys(this.raffleForm.controls).forEach((key) => {
      const control = this.raffleForm.get(key);
      if (control?.dirty) {
        if (key === 'endDate' || key === 'endTime') {
          const endDate = this.raffleForm.get('endDate')?.value;
          const endTime = this.raffleForm.get('endTime')?.value;
          modifiedData.endDate = new Date(`${endDate}T${endTime}:00`);
        } else if (key === 'newImages') {
          modifiedData.newImages = this.newImages.value;
        } else if (key === 'deleteImageIds') {
          modifiedData.deleteImageIds = this.deleteImageIds.value;
        } else {
          modifiedData[key as keyof EditRaffle] = control.value;
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
