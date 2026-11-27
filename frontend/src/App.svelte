<script lang="ts">
  import HelpModal from './lib/components/HelpModal.svelte';
  import ResourcesTab from './lib/components/ResourcesTab.svelte';
  import SavedReportTab from './lib/components/SavedReportTab.svelte';
  import ScheduleModal from './lib/components/ScheduleModal.svelte';
  import TestOwnTab from './lib/components/TestOwnTab.svelte';

  type Tab = 'resources' | 'testyourown' | 'report';

  // ?report=<id> links (from report emails) open the saved report tab.
  const reportId = new URLSearchParams(location.search).get('report');

  let tab = $state<Tab>(reportId ? 'report' : 'resources');
  let helpOpen = $state(false);
  let scheduleOpen = $state(false);
  let scheduleUrl = $state('');

  const tabs: { id: Tab; label: string }[] = [
    { id: 'resources', label: 'BrAPI Resources' },
    { id: 'testyourown', label: 'Test your own' },
    ...(reportId ? [{ id: 'report' as Tab, label: 'Saved report' }] : []),
  ];

  // Build info injected into index.html by the backend; placeholders stay as-is in dev.
  function meta(name: string) {
    const v = document.querySelector<HTMLMetaElement>(`meta[name="${name}"]`)?.content ?? '';
    return v.startsWith('${') ? 'dev' : v;
  }
  const buildDate = meta('brava-build-date');
  const buildVersion = meta('brava-build-version');

  function onTabKey(e: KeyboardEvent, index: number) {
    const step = e.key === 'ArrowRight' ? 1 : e.key === 'ArrowLeft' ? -1 : 0;
    if (!step) return;
    const next = tabs[(index + step + tabs.length) % tabs.length];
    tab = next.id;
    document.getElementById(`tab-${next.id}`)?.focus();
  }
</script>

<div class="ribbon-clip">
  <a class="ribbon" href="https://github.com/plantbreeding/IPK-BrAPI-Validator" target="_blank" rel="noopener">Fork me on GitHub</a>
</div>

<div class="page">
  <header class="hero">
    <h1><a href="./">BRAVA</a></h1>
    <p class="lead"><a href="https://www.brapi.org" target="_blank" rel="noopener">Breeding API</a> (BrAPI) Validator</p>
    <p>
      BRAVA is a tool designed to help developers test servers that comply with the
      <a href="https://github.com/plantbreeding/BrAPI" target="_blank" rel="noopener">BrAPI specifications</a>.
    </p>
    <button type="button" class="btn link small help" onclick={() => (helpOpen = true)}>How does it work?</button>
  </header>

  <div class="tabs" role="tablist" aria-label="Sections">
    {#each tabs as t, i (t.id)}
      <button
        type="button"
        role="tab"
        id="tab-{t.id}"
        aria-selected={tab === t.id}
        aria-controls="panel-{t.id}"
        tabindex={tab === t.id ? 0 : -1}
        onclick={() => (tab = t.id)}
        onkeydown={(e) => onTabKey(e, i)}
      >
        {t.label}
      </button>
    {/each}
  </div>

  <main>
    <!-- Panels stay mounted (hidden) so state like a running test survives switching tabs. -->
    <div role="tabpanel" id="panel-resources" aria-labelledby="tab-resources" hidden={tab !== 'resources'}>
      <ResourcesTab />
    </div>
    <div role="tabpanel" id="panel-testyourown" aria-labelledby="tab-testyourown" hidden={tab !== 'testyourown'}>
      <TestOwnTab
        onschedule={(url) => {
          scheduleUrl = url;
          scheduleOpen = true;
        }}
      />
    </div>
    {#if reportId}
      <div role="tabpanel" id="panel-report" aria-labelledby="tab-report" hidden={tab !== 'report'}>
        <SavedReportTab id={reportId} />
      </div>
    {/if}
  </main>
</div>

<footer class="footer">
  <div class="footer-inner">
    <div class="footer-text">
      <p>&copy; 2022 | IPK-Gatersleben. Site licensed under the MIT License.</p>
      <p>
        <small
          >Last build on {buildDate} UTC with version {buildVersion} | Send any issues/requests to the
          <a target="_blank" rel="noopener" href="https://github.com/plantbreeding/IPK-BrAPI-Validator/issues">GitHub Issues Page</a
          ></small
        >
      </p>
    </div>
    <div class="logos">
      <a href="https://www.ipk-gatersleben.de" target="_blank" rel="noopener"
        ><img src="https://www.ipk-gatersleben.de/typo3conf/ext/ipk_gatersleben/Resources/Public/Icons/ipk_logo_de.png" alt="IPK Gatersleben" /></a
      >
      <a href="https://www.leibniz-gemeinschaft.de/en/home/" target="_blank" rel="noopener"
        ><img src="https://www.ipk-gatersleben.de/fileadmin/content-ipk/Netzwerke/Netzwerke/LVB-Log.jpg" alt="Leibniz Association" /></a
      >
    </div>
  </div>
</footer>

<HelpModal bind:open={helpOpen} />
<ScheduleModal bind:open={scheduleOpen} bind:url={scheduleUrl} />

<style>
  .page {
    flex: 1;
    width: 100%;
    max-width: 1400px;
    margin: 0 auto;
    padding: 1.5rem 1rem 2rem;
  }
  .hero {
    position: relative;
    text-align: center;
    padding: 1.75rem 1rem 2.25rem;
    margin-bottom: 1.5rem;
    border-radius: var(--radius);
    background: linear-gradient(135deg, color-mix(in srgb, var(--accent) 14%, var(--surface)), var(--surface));
    border: 1px solid var(--border);
  }
  .hero h1 {
    font-size: clamp(2.5rem, 6vw, 4rem);
    font-weight: 300;
    letter-spacing: 0.06em;
    margin-bottom: 0.5rem;
  }
  .hero h1 a {
    color: inherit;
    text-decoration: none;
  }
  .hero p {
    margin: 0.25rem 0;
  }
  .lead {
    font-size: 1.2rem;
    font-weight: 300;
  }
  .help {
    position: absolute;
    right: 0.5rem;
    bottom: 0.4rem;
  }
  .tabs {
    display: flex;
    gap: 0.25rem;
    border-bottom: 1px solid var(--border);
    margin-bottom: 1.25rem;
    flex-wrap: wrap;
  }
  .tabs button {
    font: inherit;
    background: none;
    border: 1px solid transparent;
    border-bottom: none;
    border-radius: var(--radius-sm) var(--radius-sm) 0 0;
    padding: 0.55rem 1rem;
    color: var(--link);
    cursor: pointer;
    margin-bottom: -1px;
    white-space: nowrap;
  }
  .tabs button:hover {
    background: var(--surface-hover);
  }
  .tabs button[aria-selected='true'] {
    color: var(--text);
    background: var(--bg);
    border-color: var(--border);
    font-weight: 500;
  }
  .tabs button:focus-visible {
    outline: 2px solid var(--accent);
  }
  .ribbon {
    position: absolute;
    top: 2.2rem;
    right: -3.2rem;
    width: 13rem;
    transform: rotate(45deg);
    background: #aa0000;
    color: #fff;
    text-align: center;
    font-size: 0.8rem;
    font-weight: 600;
    padding: 0.3rem 0;
    box-shadow: 0 2px 4px rgb(0 0 0 / 25%);
    z-index: 10;
  }
  .ribbon:hover {
    text-decoration: none;
    background: #c00;
  }
  .ribbon-clip {
    position: absolute;
    top: 0;
    right: 0;
    width: 9rem;
    height: 9rem;
    overflow: hidden;
    pointer-events: none;
    z-index: 10;
  }
  .ribbon-clip .ribbon {
    pointer-events: auto;
  }
  .footer {
    background: var(--surface-2);
    border-top: 1px solid var(--border);
    color: var(--text-muted);
  }
  .footer-inner {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0.75rem 1rem;
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
  }
  .footer-text p {
    margin: 0;
  }
  .logos {
    display: flex;
    gap: 1rem;
  }
  .logos img {
    height: 40px;
    display: block;
    background: #fff;
    border-radius: 3px;
  }
  @media (max-width: 640px) {
    .help {
      position: static;
      margin-top: 0.5rem;
    }
    .ribbon-clip {
      display: none;
    }
  }
</style>
