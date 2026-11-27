<script lang="ts">
  import Modal from './Modal.svelte';

  let { open = $bindable(false) }: { open?: boolean } = $props();
</script>

<Modal bind:open title="How it works" wide>
  <div class="body">
    <p>BRAVA calls one or more resources in the tested server and tests the response.</p>
    <p>
      First, the <code>/calls</code> resource (BrAPI v1) or <code>/serverinfo</code> resource (BrAPI v2) is requested. If the
      response is valid, the list of available calls is parsed.
    </p>
    <p>
      Following a predefined order, BRAVA requests the available resources. For every resource, the status code, content type
      and <a href="https://json-schema.org" target="_blank" rel="noopener">JSON schema</a> is checked. Each resource has a specific
      schema defined. This checks the structure, parameter name and parameter types.
    </p>
    <p>
      For example, the <code>/germplasm-search</code> test requires the response data object to contain a
      <var>germplasmDbId</var> attribute with a string value, among others.
    </p>
    <p>
      We created the JSON schemas using the official BrAPI specifications. They are
      <a href="https://github.com/plantbreeding/IPK-BrAPI-Validator/tree/master/resources/schemas" target="_blank" rel="noopener"
        >available here</a
      >, feel free to check them and use them in your own tests. They may differ from the official specifications because we
      update them manually. Let us know of any inconsistencies in the
      <a href="https://github.com/plantbreeding/IPK-BrAPI-Validator/issues" target="_blank" rel="noopener">issue tracker</a>.
    </p>
    <p>
      Some tests also make sure the data is consistent across resources. This works by storing a few values from the response.
      The specific parameters that are saved depend on the test. The server will make a request to other resources using these
      values as input.
    </p>
    <p>This way you can automatically test some resources that would require manual input for certain parameters.</p>
    <p>
      For example, a call to <code>/markerprofiles</code> could store <var>markerprofileDbId</var>. This value can be used as
      input for <code>/markerprofiles/&#123;markerprofileDbId&#125;</code>.
    </p>
  </div>
</Modal>

<style>
  .body {
    padding: 0.25rem 1rem 0.5rem;
  }
</style>
