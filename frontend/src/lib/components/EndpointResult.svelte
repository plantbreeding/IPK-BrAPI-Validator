<script lang="ts" module>
  import type { IconName } from './Icon.svelte';
  import type { Outcome } from '../report';

  const look: Record<Outcome, { icon: IconName; color: string }> = {
    passed: { icon: 'check-circle', color: 'var(--ok)' },
    warning: { icon: 'warning', color: 'var(--warn)' },
    failed: { icon: 'times-circle', color: 'var(--fail)' },
    missingReqs: { icon: 'question-circle', color: 'var(--info)' },
    skipped: { icon: 'minus-circle', color: 'var(--skip)' },
  };
</script>

<script lang="ts">
  import type { TestItemReport } from '../types';
  import { outcomeOf } from '../report';
  import Icon from './Icon.svelte';
  import CheckResult from './CheckResult.svelte';

  let { endpoint, entry }: { endpoint: string; entry: TestItemReport | string } = $props();

  let outcome = $derived(outcomeOf(entry));
  let item = $derived(typeof entry === 'string' ? null : entry);
  let reason = $derived.by(() => {
    if (outcome === 'missingReqs') return 'missing required resources';
    if (outcome === 'skipped') return 'not in /calls';
    if (outcome === 'passed') return '';
    return typeof entry === 'string' ? entry : entry.testStatus.join(', ');
  });
</script>

{#snippet label()}
  <span class="name">
    <span style:color={look[outcome].color}><Icon name={look[outcome].icon} /></span>
    <span class="endpoint">{endpoint}</span>
    {#if item?.cached}
      <small class="muted">
        (<span class="hint" title="We save the queries for a minute to avoid spamming the remote server">cached</span>)
      </small>
    {/if}
    {#if reason}<small class="muted">({reason})</small>{/if}
  </span>
  {#if item}
    <small class="time">
      {item.responseTime}ms
      {#if endpoint === '/calls'}
        <span
          class="hint"
          title="The first call can have artificially high response time due to software overhead and it is excluded from the median."
          >(?)</span
        >
      {/if}
    </small>
  {/if}
{/snippet}

{#if item}
  <details class="endpoint-row">
    <summary class="row">{@render label()}</summary>
    <div class="checks small">
      {#each item.test as check, i (i)}
        <CheckResult {check} />
      {/each}
    </div>
  </details>
{:else}
  <div class="row static">{@render label()}</div>
{/if}

<style>
  .row {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
    padding: 0.2rem 0.25rem;
    border-radius: var(--radius-sm);
  }
  summary.row {
    cursor: pointer;
    list-style: none;
  }
  summary.row::-webkit-details-marker {
    display: none;
  }
  summary.row:hover {
    background: var(--surface-hover);
  }
  summary.row::after {
    content: '';
    flex: none;
    align-self: center;
    width: 0.45em;
    height: 0.45em;
    border-right: 2px solid var(--text-muted);
    border-bottom: 2px solid var(--text-muted);
    transform: rotate(-45deg);
    transition: transform 0.15s;
  }
  .endpoint-row[open] > summary.row::after {
    transform: rotate(45deg);
  }
  .static {
    padding-right: calc(0.45em + 0.75rem);
  }
  .name {
    display: inline-flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 0.35rem;
    margin-right: auto;
    min-width: 0;
  }
  .endpoint {
    font-family: var(--mono);
    font-size: 0.9em;
    word-break: break-all;
  }
  .time {
    flex: none;
    color: var(--text-muted);
    font-variant-numeric: tabular-nums;
  }
  .checks {
    padding: 0.2rem 0.5rem 0.4rem 1.6rem;
  }
</style>
