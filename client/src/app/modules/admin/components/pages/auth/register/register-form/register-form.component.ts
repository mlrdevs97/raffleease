import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RegisterRequest } from '../../../../../../../core/models/auth/register-request';
import { passwordMatchValidator } from '../../../../../../../core/validators/passwordMatch.validator';

@Component({
  selector: 'app-register-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './register-form.component.html',
  styleUrl: './register-form.component.css'
})
export class RegisterFormComponent {
  @Input() validationErrors: Record<string, string> = {};
  @Input() serverError: string | null = null;
  @Output() registerRequest: EventEmitter<RegisterRequest> = new EventEmitter<RegisterRequest>();
  authForm!: FormGroup;
  formSubmitted = false;

  constructor(
    private fb: FormBuilder
  ) { }

  initializeForm() {
    this.authForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [
        Validators.required,
        Validators.email
      ]],
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^\d{9}$/),
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(16),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()-_+=])[A-Za-z\d!@#$%^&*()-_+=]{8,}$/),
      ]],
      confirmPassword: ['', Validators.required],
      city: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.pattern(/^[a-zA-Z\s]+$/)
      ]],
      zipCode: ['', [
        Validators.required,
        Validators.pattern(/^\d{4,6}$/)
      ]]
    }, { validators: passwordMatchValidator });
  }

  getErrorMessage(field: string): string {
    if (this.validationErrors[field]) {
      return this.validationErrors[field];
    }

    const control = this.authForm.get(field);

    if (control?.hasError('required')) return 'Este campo es obligatorio.';
    if (control?.hasError('email')) return 'Introduce una dirección de correo válida.';
    if (control?.hasError('pattern')) {
      if (field === 'phoneNumber') return 'Introduce un número de teléfono válido (ej.: 666666666).';
      if (field === 'password') return 'La contraseña debe contener una mayúscula, una minúscula, un número y un carácter especial.';
      if (field === 'city') return 'El nombre de la ciudad solo puede contener letras y espacios.';
      if (field === 'zipCode') return 'Introduce un código postal válido (4-6 dígitos).';
    }
    if (control?.hasError('minlength')) return `La longitud mínima es de ${control.errors?.['minlength'].requiredLength} caracteres.`;
    if (control?.hasError('maxlength')) return `La longitud máxima es de ${control.errors?.['maxlength'].requiredLength} caracteres.`;
    if (field === 'confirmPassword' && this.authForm.hasError('passwordMismatch')) return 'Las contraseñas no coinciden.';

    return '';
  }

  onSubmit(event: Event) {
    event.preventDefault();

    if (this.authForm.invalid) return;

    const { name, email, phoneNumber, password, confirmPassword, city, zipCode } = this.authForm.value;

    this.registerRequest.emit({
      name: name || '',
      email: email || '',
      phoneNumber: phoneNumber || '',
      password: password || '',
      confirmPassword: confirmPassword || '',
      city: city || '',
      province: "Province",
      zipCode: zipCode || ''
    });
  }

  ngOnInit() {
    this.initializeForm();
  }
}

