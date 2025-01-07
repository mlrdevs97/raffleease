
import {
    AbstractControl,
    ValidationErrors,
    ValidatorFn,
} from '@angular/forms';

export const futureDateValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (!control.value) return null; 
  
  console.log(control.value)

  const selectedDate: Date = new Date(control.value);
  const currentDate: Date = new Date();

  if (selectedDate <= currentDate) {
    return { notFutureDate: true };
  }
  
  return null;
}
