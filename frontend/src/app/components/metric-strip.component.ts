import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';

export interface MetricStripItem {
  label: string;
  value: string | number;
  detail?: string;
  tone?: 'default' | 'accent' | 'warm';
}

@Component({
  selector: 'app-metric-strip',
  imports: [CommonModule],
  template: `
    <section class="hero-grid">
      @for (item of items(); track item.label) {
        <article class="hero-card" [class.emphasis]="item.tone === 'accent'" [class.warm]="item.tone === 'warm'">
          <span class="hero-label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          @if (item.detail) {
            <p>{{ item.detail }}</p>
          }
        </article>
      }
    </section>
  `,
  styles: [`
    .hero-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 1rem;
    }
    .hero-card {
      padding: 1.25rem;
      border: 1px solid var(--line);
      border-radius: 22px;
      background: linear-gradient(180deg, var(--card-strong), var(--card));
      box-shadow: 0 16px 38px rgba(10, 18, 32, .08);
    }
    .hero-card.emphasis {
      background: linear-gradient(135deg, var(--accent-soft), var(--card-strong));
    }
    .hero-card.warm {
      background: linear-gradient(135deg, rgba(209, 158, 91, .14), var(--card-strong));
    }
    .hero-label {
      display: inline-flex;
      font-size: .72rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: .12em;
      color: var(--muted);
    }
    .hero-card strong {
      display: block;
      margin-top: .7rem;
      font-size: clamp(1.8rem, 4vw, 3rem);
      line-height: 1;
      letter-spacing: -.05em;
    }
    .hero-card p {
      margin: .85rem 0 0;
      color: var(--muted);
      line-height: 1.55;
    }
    @media (max-width: 980px) {
      .hero-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class MetricStripComponent {
  readonly items = input.required<MetricStripItem[]>();
}
