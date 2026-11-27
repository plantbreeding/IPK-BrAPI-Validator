<script lang="ts">
  let { label = 'Loading…' }: { label?: string } = $props();
</script>

<!-- Covers the nearest positioned ancestor, like spin.js did on the forms. -->
<div class="overlay" role="status" aria-live="polite">
  <span class="ring" aria-hidden="true"></span>
  <span class="label">{label}</span>
</div>

<style>
  .overlay {
    position: absolute;
    inset: 0;
    z-index: 5;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
    background: color-mix(in srgb, var(--surface) 75%, transparent);
    backdrop-filter: blur(1px);
  }
  .ring {
    width: 2.5rem;
    height: 2.5rem;
    border-radius: 50%;
    border: 3px solid var(--border);
    border-top-color: var(--accent);
    animation: spin 0.8s linear infinite;
  }
  .label {
    color: var(--text-muted);
    font-size: 0.9em;
  }
  @keyframes spin {
    to {
      transform: rotate(1turn);
    }
  }
  @media (prefers-reduced-motion: reduce) {
    .ring {
      animation-duration: 2.4s;
    }
  }
</style>
