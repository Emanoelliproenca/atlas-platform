import { Component, input } from '@angular/core';

type ActionIconName = 'visibility' | 'edit' | 'toggle_off' | 'toggle_on' | 'file_download';

@Component({
  selector: 'app-action-icon',
  standalone: true,
  template: `
    <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18" fill="currentColor" focusable="false">
        @switch (name()) {
          @case ('visibility') {
            <path d="M12 4.5C7 4.5 2.8 7.1 1 12c1.8 4.9 6 7.5 11 7.5s9.2-2.6 11-7.5c-1.8-4.9-6-7.5-11-7.5Zm0 12.5a5 5 0 1 1 0-10 5 5 0 0 1 0 10Zm0-2a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
          }
          @case ('edit') {
            <path d="M4 17.2V20h2.8L17.9 8.9 15.1 6.1 4 17.2Zm16.1-10.4a1 1 0 0 0 0-1.4l-1.5-1.5a1 1 0 0 0-1.4 0L16 5.1 18.9 8l1.2-1.2Z" />
          }
          @case ('toggle_on') {
            <path d="M7 7h10a5 5 0 0 1 0 10H7A5 5 0 0 1 7 7Zm10 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
          }
          @case ('toggle_off') {
            <path d="M7 7h10a5 5 0 0 1 0 10H7A5 5 0 0 1 7 7Zm0 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
          }
          @case ('file_download') {
            <path d="M5 20h14v-2H5v2ZM13 4h-2v8.2L7.8 9 6.4 10.4 12 16l5.6-5.6L16.2 9 13 12.2V4Z" />
          }
        }
    </svg>
  `
})
export class ActionIconComponent {
  readonly name = input.required<ActionIconName>();
}
