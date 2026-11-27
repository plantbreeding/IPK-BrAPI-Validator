<script lang="ts">
  import type { ValidationError } from '../types';

  let { error }: { error: ValidationError } = $props();

  let lines = $derived.by(() => {
    const e = error;
    if (e.level === 'fatal') {
      return ['Error in schema that prevents further testing.', `Message: ${e.message ?? ''}`];
    }
    const out = [`Error: ${e.domain ?? ''} - ${e.keyword ?? ''}`];
    if (e.instance) out.push(`In element: ${e.instance.pointer ?? ''}`);
    if (e.expected) out.push(`Expected: ${e.expected.join(', ')}`);
    if (e.minItems) out.push(`Minimum items: ${e.minItems}`);
    if (e.unwanted) out.push(`Invalid items: ${e.unwanted.join(', ')}`);
    if (e.found) out.push(`Found: ${e.found}`);
    if (e.missing) out.push(`Missing: ${e.missing.join(', ')}`);
    out.push(`Message: ${e.message ?? ''}`);
    return out;
  });
</script>

<pre>{lines.join('\n')}</pre>

<style>
  pre {
    font-family: var(--mono);
    font-size: 12px;
    white-space: pre-wrap;
    word-break: break-word;
    margin: 0.4rem 0;
    padding: 0.45rem 0.6rem;
    background: var(--code-bg);
    border: 1px solid var(--border);
    border-radius: var(--radius-sm);
  }
</style>
