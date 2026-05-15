import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-page-header',
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-header-shell">
      <div>
        <p class="section-eyebrow">{{ eyebrow() }}</p>
        <h2>{{ title() }}</h2>
        @if (copy()) {
          <p class="section-copy">{{ copy() }}</p>
        }
      </div>

      @if (backLink()) {
        <a [routerLink]="backLink()" class="back-link">{{ backLabel() }}</a>
      }
    </div>
  `,
  styles: [`
    .page-header-shell {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
    }
    .section-eyebrow {
      margin: 0 0 .75rem;
      text-transform: uppercase;
      letter-spacing: .16em;
      font-size: .76rem;
      color: var(--accent);
      font-weight: 700;
    }
    h2 {
      margin: 0;
      font-family: inherit;
      font-weight: 760;
      line-height: 1.02;
    }
    .section-copy {
      margin: .75rem 0 0;
      max-width: 48rem;
      color: var(--muted);
      line-height: 1.6;
    }
    .back-link {
      color: var(--accent);
      font-weight: 700;
      text-decoration: none;
      white-space: nowrap;
    }
    @media (max-width: 720px) {
      .page-header-shell {
        align-items: flex-start;
        flex-direction: column;
      }
    }
  `]
})
export class PageHeaderComponent {
  readonly eyebrow = input.required<string>();
  readonly title = input.required<string>();
  readonly copy = input('');
  readonly backLabel = input('Voltar');
  readonly backLink = input<string | null>(null);
}
