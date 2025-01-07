import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-modals',
  standalone: true,
  imports: [NgClass],
  templateUrl: './modals.component.html',
  styleUrl: './modals.component.css'
})
export class ModalsComponent {
  @Input() action!: 'publish' | 'pause' | 'restart' | 'delete' | null;
  @Input() raffleId!: number;
  @Input() display!: boolean;
  @Output() confirmed: EventEmitter<void> = new EventEmitter<void>();
  @Output() closed: EventEmitter<void> = new EventEmitter<void>();
  header!: string;
  body!: string;

  confirm() {
    this.confirmed.emit();
  }

  close() {
    this.closed.emit();
  }

  private setModalTexts() {
    switch (this.action) {
      case 'publish':
        this.header = 'Confirmar publicación';
        this.body = '¿Estás seguro de que deseas publicar?';
        break;
      case 'pause':
        this.header = 'Confirmar pausa';
        this.body = '¿Estás seguro de que deseas pausar?';
        break;
      case 'restart':
        this.header = 'Confirmar reinicio';
        this.body = '¿Estás seguro de que deseas reiniciar?';
        break;
      case 'delete':
        this.header = 'Confirmar eliminación';
        this.body = '¿Estás seguro de que deseas eliminar?';
        break;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['action']) this.setModalTexts();
  }
}
