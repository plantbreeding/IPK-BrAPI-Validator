<script lang="ts">
  import { getTestReport } from '../api';
  import { formatDate } from '../report';
  import ReportView from './ReportView.svelte';
  import Spinner from './Spinner.svelte';

  let { id }: { id: string } = $props();

  let request = $derived(getTestReport(id));
</script>

<div class="wrap">
  {#await request}
    <div class="loading"><Spinner label="Loading report…" /></div>
  {:then report}
    <ReportView report={report.shortReport}>
      {#snippet header()}
        <span class="card-title">Test result: {report.resourceUrl ?? ''}</span>
        <small><em>{formatDate(report.date)}</em></small>
      {/snippet}
    </ReportView>
  {:catch}
    <ReportView report={null} message="This report is not available anymore.">
      {#snippet header()}
        <span class="card-title">Test result:</span>
      {/snippet}
    </ReportView>
  {/await}
</div>

<style>
  .wrap {
    max-width: 58rem;
    margin: 0 auto;
  }
  .loading {
    position: relative;
    height: 8rem;
  }
</style>
