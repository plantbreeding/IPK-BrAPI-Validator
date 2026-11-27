<script lang="ts">
  import { getBrapiVersions, testCall, testCallErrorMessage } from '../api';
  import { firstCallUrl, formatDate } from '../report';
  import type { StoredReport } from '../types';
  import Icon from './Icon.svelte';
  import ReportView from './ReportView.svelte';
  import Spinner from './Spinner.svelte';

  let { onschedule }: { onschedule: (url: string) => void } = $props();

  let url = $state('');
  let versions = $state<string[]>([]);
  let brapiversion = $state('');
  let accessToken = $state('');
  let strict = $state(false);

  let running = $state(false);
  let error = $state('');
  let result = $state.raw<StoredReport | null>(null);
  let controller: AbortController | null = null;

  getBrapiVersions()
    .then((v) => {
      versions = v;
      brapiversion = v.at(-1) ?? '';
    })
    .catch(() => (error = 'The list of BrAPI versions could not be loaded.'));

  let testedUrl = $derived(firstCallUrl(url, brapiversion));

  async function submit(e: SubmitEvent) {
    e.preventDefault();
    controller = new AbortController();
    running = true;
    error = '';
    try {
      result = await testCall({ url, brapiversion, accessToken, strict }, controller.signal);
    } catch (err) {
      if (!(err instanceof DOMException && err.name === 'AbortError')) error = testCallErrorMessage(err);
    } finally {
      running = false;
      controller = null;
    }
  }
</script>

<div class={['layout', result && 'with-result']}>
  <section class="card form-card">
    <div class="card-header">
      <span class="card-title"><Icon name="edit" /> Test a BrAPI server</span>
    </div>
    <form class="card-body" onsubmit={submit}>
      {#if running}
        <Spinner label="Testing the server, this may take a few minutes…" />
      {/if}
      <div class="field">
        <label for="serverurl">Server URL</label>
        <input
          type="url"
          id="serverurl"
          placeholder="https://test-server.brapi.org/brapi/v2/"
          aria-describedby="serverHelp"
          required
          bind:value={url}
        />
        <small id="serverHelp" class="help"
          >Enter your own server or test the public servers available in the official BrAPI repository.</small
        >
      </div>
      <div class="field">
        <label for="brapiversion">BrAPI Version</label>
        <select id="brapiversion" bind:value={brapiversion}>
          {#each versions as v (v)}
            <option value={v}>{v}</option>
          {/each}
        </select>
      </div>
      <div class="field">
        <label for="accessToken">
          OAuth2 Access token for OAuth2 protected BrAPI (<code>Authorization: Bearer &lt;TOKEN&gt;</code>)
        </label>
        <input type="text" id="accessToken" autocomplete="off" aria-describedby="tokenHelp" bind:value={accessToken} />
        <small id="tokenHelp" class="help"
          >Enter a valid OAuth2 access token that should be used by BRAVA to make requests to your BrAPI server.</small
        >
      </div>
      <label class="check">
        <input type="checkbox" bind:checked={strict} />
        <span>
          Strict test (<span
            class="hint"
            title="When checked, the validator will not allow extra fields in every response object. Disabling it might hide legitimate errors such as typos on optional fields."
            >?</span
          >)
        </span>
      </label>
      <div class="field tested">
        <span class="field-label">Tested URL</span>
        <a class="url" target="_blank" rel="noopener" href={testedUrl}>{testedUrl}</a>
      </div>
      {#if error}
        <div class="alert error" role="alert">{error}</div>
      {/if}
      <div class="actions">
        <button type="submit" class="btn primary" disabled={running}>Test it now! (it may take a few minutes)</button>
        {#if running}
          <button type="button" class="btn" onclick={() => controller?.abort()}>Cancel</button>
        {/if}
        <button type="button" class="btn schedule" onclick={() => onschedule(url)}>Schedule a periodic test</button>
      </div>
    </form>
  </section>

  {#if result}
    <div>
      <ReportView report={result.shortReport}>
        {#snippet header()}
          <span class="card-title">Test result</span>
          <small><em>{formatDate(result!.date)}</em></small>
        {/snippet}
      </ReportView>
    </div>
  {/if}
</div>

<style>
  .layout {
    display: grid;
    grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
    gap: 1.25rem;
    align-items: start;
  }
  @media (max-width: 1199px) {
    .layout {
      grid-template-columns: minmax(0, 1fr);
    }
  }
  form {
    position: relative;
  }
  .tested {
    margin-top: 0.75rem;
  }
  .url {
    font-size: 14px;
    word-break: break-all;
  }
  .actions {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  .schedule {
    margin-left: auto;
  }
</style>
