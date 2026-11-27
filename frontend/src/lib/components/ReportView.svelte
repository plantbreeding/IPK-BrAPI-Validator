<script lang="ts">
  import type { Snippet } from 'svelte';
  import type { ShortReport } from '../types';
  import { median, outcomeOf, responseTimes } from '../report';
  import EndpointResult from './EndpointResult.svelte';

  let {
    report,
    header,
    message = '',
  }: {
    report: ShortReport | null | undefined;
    /** Content of the card header (title, date, selectors). */
    header: Snippet;
    /** Shown instead of the report, e.g. when it could not be loaded. */
    message?: string;
  } = $props();

  let showSkipped = $state(false);

  let folders = $derived(
    Object.entries(report ?? {}).map(([name, tests]) => {
      const entries = Object.entries(tests).map(([endpoint, entry]) => ({
        endpoint,
        entry,
        outcome: outcomeOf(entry),
      }));
      const counted = entries.filter((e) => e.outcome !== 'skipped');
      return {
        name,
        entries,
        passed: counted.filter((e) => e.outcome === 'passed').length,
        total: counted.length,
        allSkipped: counted.length === 0,
      };
    }),
  );

  let medianTime = $derived(report ? median(responseTimes(report)) : '-');
</script>

<section class="card">
  <div class="card-header">{@render header()}</div>
  {#if message}
    <p class="empty"><strong>{message}</strong></p>
  {:else if report}
    <div class="toolbar">
      <button type="button" class="btn link small" aria-pressed={showSkipped} onclick={() => (showSkipped = !showSkipped)}>
        {showSkipped ? 'Hide' : 'Show'} tests not present in the resource /calls
      </button>
      <small class="muted">Median response time: {medianTime}ms</small>
    </div>
    <ul class="folders">
      {#each folders as folder (folder.name)}
        {#if showSkipped || !folder.allSkipped}
          <li>
            <details open>
              <summary>
                <strong>{folder.name}</strong>
                <span class="muted">({folder.passed}/{folder.total})</span>
              </summary>
              <div class="entries">
                {#each folder.entries as e (e.endpoint)}
                  {#if showSkipped || e.outcome !== 'skipped'}
                    <EndpointResult endpoint={e.endpoint} entry={e.entry} />
                  {/if}
                {/each}
              </div>
            </details>
          </li>
        {/if}
      {/each}
    </ul>
  {:else}
    <p class="empty muted">No report available.</p>
  {/if}
</section>

<style>
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.25rem 1rem;
    padding: 0.3rem 1rem 0.3rem 0.5rem;
    border-bottom: 1px solid var(--border);
  }
  .folders {
    list-style: none;
    margin: 0;
    padding: 0;
  }
  .folders > li {
    border-bottom: 1px solid var(--border);
  }
  .folders > li:last-child {
    border-bottom: none;
  }
  summary {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    padding: 0.4rem 1rem;
    cursor: pointer;
    list-style: none;
  }
  summary::-webkit-details-marker {
    display: none;
  }
  summary:hover {
    background: var(--surface-hover);
  }
  summary::after {
    content: '';
    margin-left: auto;
    width: 0.5em;
    height: 0.5em;
    border-right: 2px solid var(--text-muted);
    border-bottom: 2px solid var(--text-muted);
    transform: rotate(-45deg);
    transition: transform 0.15s;
  }
  details[open] > summary::after {
    transform: rotate(45deg);
  }
  .entries {
    padding: 0 0.75rem 0.5rem 1rem;
  }
  .empty {
    padding: 1rem;
    margin: 0;
  }
</style>
