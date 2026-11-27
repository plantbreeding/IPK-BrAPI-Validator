<script lang="ts">
  import type { Snippet } from 'svelte';
  import Icon from './Icon.svelte';

  let {
    open = $bindable(false),
    title,
    wide = false,
    children,
  }: { open?: boolean; title: string; wide?: boolean; children: Snippet } = $props();

  // Keeps the native <dialog> in sync with `open` (showModal() gives focus trap, Esc and backdrop).
  function sync(dialog: HTMLDialogElement) {
    if (open && !dialog.open) dialog.showModal();
    else if (!open && dialog.open) dialog.close();
  }
</script>

<dialog
  {@attach sync}
  class={{ wide }}
  aria-label={title}
  onclose={() => (open = false)}
  onclick={(e) => {
    // A click on the backdrop targets the dialog element itself.
    if (e.target === e.currentTarget) open = false;
  }}
>
  <div class="content">
    <header>
      <h2>{title}</h2>
      <button type="button" class="btn link icon" aria-label="Close" onclick={() => (open = false)}>
        <Icon name="times" size="1.1em" />
      </button>
    </header>
    {@render children()}
  </div>
</dialog>

<style>
  dialog {
    width: min(500px, calc(100vw - 2rem));
    max-height: calc(100vh - 2rem);
    padding: 0;
    border: 1px solid var(--border);
    border-radius: var(--radius);
    background: var(--surface);
    color: var(--text);
    box-shadow: 0 10px 40px rgb(0 0 0 / 25%);
  }
  dialog.wide {
    width: min(800px, calc(100vw - 2rem));
  }
  dialog::backdrop {
    background: rgb(10 15 20 / 50%);
  }
  .content {
    position: relative;
  }
  header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.85rem 1rem;
    border-bottom: 1px solid var(--border);
  }
  h2 {
    font-size: 1.15rem;
    font-weight: 600;
  }
</style>
