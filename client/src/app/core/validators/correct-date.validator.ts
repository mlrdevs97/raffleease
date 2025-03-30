import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const correctDateValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (!control.value) return null;

  const selectedDate: Date = new Date(control.value);
  const currentDate: Date = new Date();
  const oneYearLater: Date = new Date(currentDate);
  oneYearLater.setFullYear(currentDate.getFullYear() + 1);

  if (selectedDate <= currentDate) {
    return { notFutureDate: true };
  }

  if (selectedDate > oneYearLater) {
    return { exceedsOneYear: true };
  }

  return null;
};
