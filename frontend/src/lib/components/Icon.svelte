<script lang="ts" module>
  // Small inline SVG icon set (24x24, stroked), replacing Font Awesome.
  const paths = {
    'check-circle': '<circle cx="12" cy="12" r="9"/><path d="m8 12.5 2.8 2.8L16.5 9.5"/>',
    'times-circle': '<circle cx="12" cy="12" r="9"/><path d="m9 9 6 6m0-6-6 6"/>',
    'question-circle':
      '<circle cx="12" cy="12" r="9"/><path d="M9.6 9.3a2.5 2.5 0 0 1 4.8.9c0 1.7-2.4 2.2-2.4 3.6"/><path d="M12 17h.01"/>',
    'minus-circle': '<circle cx="12" cy="12" r="9"/><path d="M8 12h8"/>',
    warning: '<path d="M12 3.5 2.5 20h19z"/><path d="M12 10v4.5M12 17.2h.01"/>',
    check: '<path d="m5 12.5 4.5 4.5L19 7.5"/>',
    times: '<path d="m6 6 12 12M18 6 6 18"/>',
    table: '<rect x="3" y="4.5" width="18" height="15" rx="2"/><path d="M3 9.5h18M3 14.5h18M9 9.5v10"/>',
    edit: '<path d="M4 20h4L19 9l-4-4L4 16z"/><path d="m13.5 6.5 4 4"/>',
    chevron: '<path d="m9 6 6 6-6 6"/>',
    frown: '<circle cx="12" cy="12" r="9"/><path d="M8.5 16.5a5 5 0 0 1 7 0M9 10h.01M15 10h.01"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    sort: '<path d="m8 9 4-4 4 4M8 15l4 4 4-4"/>',
    'sort-up': '<path d="m8 14 4-4 4 4"/>',
    'sort-down': '<path d="m8 10 4 4 4-4"/>',
  } as const;

  export type IconName = keyof typeof paths;
</script>

<script lang="ts">
  let { name, size = '1em', label }: { name: IconName; size?: string; label?: string } = $props();
</script>

<svg
  class="icon"
  width={size}
  height={size}
  viewBox="0 0 24 24"
  fill="none"
  stroke="currentColor"
  stroke-width="2"
  stroke-linecap="round"
  stroke-linejoin="round"
  role={label ? 'img' : undefined}
  aria-label={label}
  aria-hidden={label ? undefined : 'true'}
>
  {@html paths[name]}
</svg>

<style>
  .icon {
    flex: none;
    vertical-align: -0.125em;
  }
</style>
