import { Component, computed, inject, input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { appIcons, AppIconName } from '../core/icons/icons';

@Component({
  selector: 'app-icon',
  standalone: true,
  template: `<span class="inline-flex" [innerHTML]="svg()"></span>`
})
export class AppIconComponent {
  private readonly sanitizer = inject(DomSanitizer);

  readonly name = input.required<AppIconName>();
  readonly className = input('h-5 w-5');

  readonly svg = computed(() => {
    const icon = appIcons[this.name()];
    const markup = `<svg viewBox="0 0 ${icon.width ?? 24} ${icon.height ?? 24}" class="${this.className()}" fill="none" xmlns="http://www.w3.org/2000/svg">${icon.body}</svg>`;
    return this.sanitizer.bypassSecurityTrustHtml(markup);
  });
}
