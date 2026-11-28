<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const model = defineModel<string>({ required: true })

const props = defineProps<{
  id: string
  label: string
  hint?: string
}>()

const hintId = `${props.id}-hint`
</script>

<template>
  <!-- Label, hint and classes mirror FormField: change both together. -->
  <div class="space-y-1">
    <label :for="id" class="block text-sm font-medium text-slate-300">{{ label }}</label>

    <textarea
      :id="id"
      v-model="model"
      v-bind="$attrs"
      :aria-describedby="hint ? hintId : undefined"
      rows="4"
      class="w-full resize-y rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 focus:outline-none focus:ring-2 focus:ring-slate-400"
    />

    <p v-if="hint" :id="hintId" class="text-xs text-slate-400">{{ hint }}</p>
  </div>
</template>