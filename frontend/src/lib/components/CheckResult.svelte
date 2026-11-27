<script lang="ts">
  import type { TestExecReport } from '../types';
  import Icon from './Icon.svelte';
  import ValidationErrorView from './ValidationErrorView.svelte';

  let { check }: { check: TestExecReport } = $props();

  // Schema paths are absolute ("/schemas/v2.0/..."); the vanilla UI links them relative to the app root.
  let schemaHref = $derived(check.schema ? `.${check.schema}` : '');
</script>

<details>
  <summary class={check.passed ? 'ok' : 'fail'}>
    <Icon name={check.passed ? 'check' : 'times'} />
    <span>Test: {check.name}</span>
  </summary>
  <div class="body">
    {#if check.message.length > 0}
      <p class="message">{check.message.join('\n')}</p>
    {/if}
    {#if check.schema}
      <p>Schema: <a target="_blank" rel="noopener" href={schemaHref}>{check.schema}</a></p>
    {/if}
    {#if check.error.length > 0}
      <p><strong>Validation Errors:</strong> <span class="muted">(only showing top 10)</span></p>
      {#each check.error as error, i (i)}
        <ValidationErrorView {error} />
      {/each}
    {/if}
  </div>
</details>

<style>
  summary {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    cursor: pointer;
    padding: 0.15rem 0;
    list-style: none;
  }
  summary::-webkit-details-marker {
    display: none;
  }
  summary::after {
    content: '';
    margin-left: auto;
    width: 0.45em;
    height: 0.45em;
    border-right: 2px solid currentColor;
    border-bottom: 2px solid currentColor;
    transform: rotate(-45deg);
    transition: transform 0.15s;
    opacity: 0.6;
  }
  details[open] > summary::after {
    transform: rotate(45deg);
  }
  .ok {
    color: var(--ok);
  }
  .fail {
    color: var(--fail);
  }
  .body {
    padding: 0.35rem 0.75rem 0.5rem;
    margin: 0.15rem 0 0.4rem;
    background: var(--surface-2);
    border-radius: var(--radius-sm);
  }
  .body p {
    margin: 0.3rem 0;
    word-break: break-word;
  }
  .message {
    white-space: pre-wrap;
  }
</style>
