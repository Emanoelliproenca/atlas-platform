import { Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-login-panel',
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './login-panel.component.html',
  styleUrl: './login-panel.component.scss'
})
export class LoginPanelComponent {
  readonly autenticado = input.required<boolean>();
  readonly baseUrl = input.required<string>();
  readonly username = input.required<string>();
  readonly password = input.required<string>();
  readonly sessaoRestante = input.required<string>();
  readonly erro = input.required<string>();
  readonly carregando = input.required<boolean>();
  readonly ehAdmin = input.required<boolean>();

  readonly baseUrlChange = output<string>();
  readonly usernameChange = output<string>();
  readonly passwordChange = output<string>();
  readonly entrar = output<void>();
  readonly atualizar = output<void>();
  readonly sair = output<void>();

  protected readonly mostrarSenha = signal(false);
  protected readonly formularioValido = computed(() =>
    Boolean(this.username().trim() && this.password().trim())
  );

  protected alternarSenha(): void {
    this.mostrarSenha.set(!this.mostrarSenha());
  }

  protected solicitarEntrada(): void {
    if (!this.formularioValido() || this.carregando()) {
      return;
    }

    this.entrar.emit();
  }
}
