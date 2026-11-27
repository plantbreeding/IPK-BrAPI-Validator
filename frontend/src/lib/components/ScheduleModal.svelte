<script lang="ts">
  import { ApiError, scheduleTest } from '../api';
  import type { Frequency } from '../types';
  import Icon from './Icon.svelte';
  import Modal from './Modal.svelte';
  import Spinner from './Spinner.svelte';

  let { open = $bindable(false), url = $bindable('') }: { open?: boolean; url?: string } = $props();

  let email = $state('');
  let frequency = $state<Frequency>('weekly');
  let submitToRepo = $state(false);
  let crop = $state('');

  let sending = $state(false);
  let success = $state<string | null>(null);
  let failure = $state<string | null>(null);

  async function submit(e: SubmitEvent) {
    e.preventDefault();
    sending = true;
    success = failure = null;
    try {
      const res = await scheduleTest({ 'base-url': url, email, crop, frequency, submitToRepo });
      success = res?.message ?? '';
    } catch (err) {
      failure = err instanceof ApiError ? err.message : '';
    } finally {
      sending = false;
    }
  }
</script>

<Modal bind:open title="Set up a periodic report">
  <form onsubmit={submit}>
    <div class="body">
      {#if sending}
        <Spinner label="Submitting…" />
      {/if}
      <p>Register your endpoint here and we'll test it periodically.</p>
      <p>You'll get an email where you can see at a glance how your BrAPI server development is going.</p>
      <div class="field">
        <label for="serverUrlModal">Server URL</label>
        <input type="url" id="serverUrlModal" placeholder="https://www.brapiserver.com/brapi/v2/" required bind:value={url} />
      </div>
      <div class="field">
        <label for="emailModal">Email</label>
        <input
          type="email"
          id="emailModal"
          placeholder="dev@brapiserver.com"
          autocomplete="email"
          aria-describedby="emailHelp"
          required
          bind:value={email}
        />
        <small id="emailHelp" class="help">You can stop the test schedule using the unsubscribe link in the test report e-mails</small>
      </div>
      <fieldset>
        <legend class="field-label">Frequency</legend>
        {#each [['daily', 'Daily report'], ['weekly', 'Weekly report'], ['monthly', 'Monthly report']] as [value, label] (value)}
          <label class="check">
            <input type="radio" name="frequency" {value} bind:group={frequency} />
            <span>{label}</span>
          </label>
        {/each}
      </fieldset>
      <fieldset>
        <legend class="field-label">Add to resource list</legend>
        <label class="check">
          <input type="checkbox" bind:checked={submitToRepo} />
          <span>Check this if you want to submit your server to be added to the resource list.</span>
        </label>
      </fieldset>
      {#if submitToRepo}
        <div class="field">
          <label for="cropSpecies">Crop species</label>
          <input type="text" id="cropSpecies" placeholder="wheat, barley, multi-crop..." required bind:value={crop} />
        </div>
      {/if}
      {#if success !== null}
        <div class="alert success" role="status"><Icon name="check" /> <span>Got it! {success}</span></div>
      {/if}
      {#if failure !== null}
        <div class="alert error" role="alert"><Icon name="frown" /> <span>Oops! There was something wrong. {failure}</span></div>
      {/if}
    </div>
    <footer>
      <button type="button" class="btn" onclick={() => (open = false)}>Close</button>
      <button type="submit" class="btn primary" disabled={sending}>Submit</button>
    </footer>
  </form>
</Modal>

<style>
  .body {
    position: relative;
    padding: 1rem;
  }
  .body > p {
    margin: 0 0 0.75rem;
  }
  fieldset {
    border: 0;
    padding: 0;
    margin: 0 0 1rem;
  }
  legend {
    padding: 0;
    margin-bottom: 0.3rem;
  }
  footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    border-top: 1px solid var(--border);
  }
</style>
