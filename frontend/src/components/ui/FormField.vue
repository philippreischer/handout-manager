<script setup lang="ts">
const model = defineModel<string>({ required: true })

defineProps<{
  id: string
  label: string
}>()

defineOptions({ inheritAttrs: false })
</script>

<template>
  <div>
    <label :for="id" class="mb-1 block text-sm text-slate-400">{{ label }}</label>

    <div class="relative">
      <input
        :id="id"
        v-model="model"
        v-bind="$attrs"
        class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 placeholder-slate-500 focus:border-emerald-500 focus:outline-none"
        :class="{ 'pr-16': $slots.trailing }"
      />

      <div v-if="$slots.trailing" class="absolute top-1/2 right-2 -translate-y-1/2">
        <slot name="trailing" />
      </div>
    </div>
  </div>
</template>