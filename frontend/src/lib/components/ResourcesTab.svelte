<script lang="ts">
  import { getPublicResources } from '../api';
  import { formatDate, stats, type Stats } from '../report';
  import type { Resource } from '../types';
  import Icon from './Icon.svelte';
  import ReportView from './ReportView.svelte';
  import Spinner from './Spinner.svelte';

  interface Row {
    resource: Resource;
    id: string;
    name: string;
    description: string;
    provider: string;
    crop: string;
    url: string;
    status: Stats;
  }

  type SortKey = 'name' | 'provider' | 'crop' | 'status';

  let rows = $state.raw<Row[]>([]);
  let loading = $state(true);
  let loadError = $state('');
  let selectedId = $state<string | null>(null);
  let reportIndex = $state(0);
  let expanded = $state<Record<string, boolean>>({});
  let sortKey = $state<SortKey>('name');
  let sortAsc = $state(true);

  getPublicResources()
    .then((res) => {
      rows = res.map((r) => ({
        resource: r,
        id: r.id,
        name: r.name,
        description: r.description ?? '',
        provider: r.provider?.name ?? '',
        crop: r.crop ?? '',
        url: r.url ?? r['base-url'] ?? '',
        status: stats(r.lastTestReports[0]?.shortReport),
      }));
      const first = [...rows].sort((a, b) => a.name.localeCompare(b.name))[0];
      if (first) selectedId = first.id;
    })
    .catch(() => (loadError = 'The resource list could not be loaded.'))
    .finally(() => (loading = false));

  // Status sorts by passed tests, then by total reversed (same as the vanilla FooTable sort value).
  const statusValue = (r: Row) => r.status.passed - r.status.total * 0.001;

  let sorted = $derived.by(() => {
    const dir = sortAsc ? 1 : -1;
    return [...rows].sort((a, b) =>
      sortKey === 'status'
        ? (statusValue(a) - statusValue(b)) * dir
        : a[sortKey].localeCompare(b[sortKey], undefined, { sensitivity: 'base' }) * dir,
    );
  });

  let selected = $derived(rows.find((r) => r.id === selectedId) ?? null);
  let reports = $derived(selected?.resource.lastTestReports ?? []);
  let report = $derived(reports[reportIndex] ?? null);

  function sortBy(key: SortKey) {
    if (sortKey === key) sortAsc = !sortAsc;
    else {
      sortKey = key;
      sortAsc = true;
    }
  }

  function select(id: string) {
    selectedId = id;
    reportIndex = 0;
  }

  const shortUrl = (url: string) => (url.length > 45 ? url.slice(0, 45) + '…' : url);
</script>

{#snippet sortHeader(key: SortKey, label: string, cls = '')}
  <th
    scope="col"
    class={cls}
    aria-sort={sortKey === key ? (sortAsc ? 'ascending' : 'descending') : 'none'}
  >
    <button type="button" class="sort" onclick={() => sortBy(key)}>
      {label}
      <Icon name={sortKey !== key ? 'sort' : sortAsc ? 'sort-up' : 'sort-down'} />
    </button>
  </th>
{/snippet}

<div class="layout">
  <section class="card table-card">
    <div class="card-header">
      <span class="card-title">
        <Icon name="table" /> BrAPI Resources
        <small class="muted"
          >taken from the <a href="https://github.com/plantbreeding/API/blob/master/brapi-resources.json" target="_blank" rel="noopener"
            >BrAPI repository</a
          ></small
        >
      </span>
    </div>
    <div class="table-wrap">
      {#if loading}
        <Spinner label="Loading resources…" />
      {/if}
      <table>
        <thead>
          <tr>
            <th class="expander" aria-label="Details"></th>
            {@render sortHeader('name', 'Name')}
            {@render sortHeader('provider', 'Provider', 'md-up')}
            {@render sortHeader('crop', 'Crop', 'sm-up')}
            <th scope="col" class="status-col" aria-sort={sortKey === 'status' ? (sortAsc ? 'ascending' : 'descending') : 'none'}>
              <button type="button" class="sort" onclick={() => sortBy('status')}>
                Status
                <Icon name={sortKey !== 'status' ? 'sort' : sortAsc ? 'sort-up' : 'sort-down'} />
              </button>
              <small class="muted sub">passed/warning/total</small>
            </th>
          </tr>
        </thead>
        <tbody>
          {#each sorted as row (row.id)}
            <tr class={{ active: row.id === selectedId }}>
              <td class="expander">
                <button
                  type="button"
                  class="btn link icon toggle"
                  aria-expanded={!!expanded[row.id]}
                  aria-label="Show details for {row.name}"
                  onclick={() => (expanded[row.id] = !expanded[row.id])}
                >
                  <Icon name="chevron" />
                </button>
              </td>
              <td>{row.name}</td>
              <td class="md-up">{row.provider}</td>
              <td class="sm-up">{row.crop}</td>
              <td class="status-col">
                <span class="status">
                  <span>
                    <strong class="ok">{row.status.passed}</strong>/<strong class="warn">{row.status.warning}</strong>/<strong
                      >{row.status.total}</strong
                    >
                    <small>tests</small>
                    <small class="hint muted" title="Median response time, excluding /calls.">{row.status.respTime}ms</small>
                  </span>
                  <button
                    type="button"
                    class={['btn', 'icon', 'show', row.id === selectedId && 'primary']}
                    aria-label="Show report of {row.name}"
                    aria-pressed={row.id === selectedId}
                    onclick={() => select(row.id)}
                  >
                    <Icon name="chevron" />
                  </button>
                </span>
              </td>
            </tr>
            {#if expanded[row.id]}
              <tr class="details">
                <td></td>
                <td colspan="4">
                  <dl>
                    <div class="md-down"><dt>Provider</dt><dd>{row.provider || '–'}</dd></div>
                    <div class="sm-down"><dt>Crop</dt><dd>{row.crop || '–'}</dd></div>
                    <div><dt>Description</dt><dd>{row.description || '–'}</dd></div>
                    <div>
                      <dt>Base url</dt>
                      <dd><a target="_blank" rel="noopener" href={row.url} title={row.url}>{shortUrl(row.url)}</a></dd>
                    </div>
                  </dl>
                </td>
              </tr>
            {/if}
          {:else}
            {#if !loading}
              <tr><td colspan="5" class="muted empty">{loadError || 'No resources registered.'}</td></tr>
            {/if}
          {/each}
        </tbody>
      </table>
    </div>
  </section>

  <div class="report-col">
    <ReportView report={report?.shortReport}>
      {#snippet header()}
        <strong class="card-title">{selected?.name ?? 'Test report'}</strong>
        {#if reports.length > 0}
          <select class="date" aria-label="Report date" bind:value={reportIndex}>
            {#each reports as r, i (i)}
              <option value={i}>{formatDate(r.date)}</option>
            {/each}
          </select>
        {/if}
      {/snippet}
    </ReportView>
  </div>
</div>

<style>
  .layout {
    display: grid;
    grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
    gap: 1.25rem;
    align-items: start;
  }
  @media (max-width: 991px) {
    .layout {
      grid-template-columns: minmax(0, 1fr);
    }
  }
  .table-wrap {
    position: relative;
    min-height: 6rem;
    overflow-x: auto;
  }
  table {
    width: 100%;
    border-collapse: collapse;
  }
  th,
  td {
    text-align: left;
    padding: 0.5rem 0.6rem;
    border-bottom: 1px solid var(--border);
    vertical-align: middle;
  }
  th {
    vertical-align: top;
    font-weight: 600;
    white-space: nowrap;
  }
  tbody tr:not(.details):hover {
    background: var(--surface-hover);
  }
  tr.active {
    background: color-mix(in srgb, var(--accent) 12%, transparent);
  }
  tr.active:hover {
    background: color-mix(in srgb, var(--accent) 18%, transparent) !important;
  }
  .sort {
    all: unset;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
  }
  .sort:focus-visible {
    outline: 2px solid var(--accent);
  }
  .sub {
    display: block;
    font-weight: 400;
  }
  .expander {
    width: 2.25rem;
    padding-right: 0;
  }
  .toggle :global(svg) {
    transition: transform 0.15s;
  }
  .toggle[aria-expanded='true'] :global(svg) {
    transform: rotate(90deg);
  }
  .status-col {
    width: 210px;
  }
  .status {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    white-space: nowrap;
  }
  .ok {
    color: var(--ok);
  }
  .warn {
    color: var(--warn);
  }
  .details td {
    background: var(--surface-2);
    padding-top: 0.4rem;
  }
  dl {
    margin: 0;
    display: grid;
    gap: 0.3rem;
  }
  dl > div {
    display: grid;
    grid-template-columns: 7rem 1fr;
    gap: 0.5rem;
  }
  dt {
    font-weight: 600;
  }
  dd {
    margin: 0;
    word-break: break-word;
  }
  .empty {
    text-align: center;
    padding: 1.5rem;
  }
  .date {
    width: auto;
    padding: 0.3rem 0.5rem;
  }
  /* Responsive columns: hidden columns move into the details row (like FooTable breakpoints). */
  .md-down,
  .sm-down {
    display: none !important;
  }
  @media (max-width: 767px) {
    .md-up {
      display: none;
    }
    .md-down {
      display: grid !important;
    }
  }
  @media (max-width: 575px) {
    .sm-up {
      display: none;
    }
    .sm-down {
      display: grid !important;
    }
  }
</style>
