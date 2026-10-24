<script setup lang="ts">
import { computed } from 'vue'

const model = defineModel<string>({ required: true })

const props = defineProps<{
  id: string
  label: string
}>()

// Everything not declared above - type, required, minlength, autocomplete - is
// bound to the input below. Without this the wrapper div would carry it as well.
defineOptions({ inheritAttrs: false })

// Ties the hint to the input, so a screen reader reads it as part of the field
// instead of as unrelated text next to it.
const hintId = computed(() => `${props.id}-hint`)
</script>

<template>
  <div>
    <label :for="id" class="mb-1 block text-sm text-slate-400">{{ label }}</label>

    <div class="relative">
      <input
        :id="id"
        v-model="model"
        v-bind="$attrs"
        :aria-describedby="$slots.hint ? hintId : undefined"
        class="w-full rounded-md border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 placeholder-slate-500 focus:border-emerald-500 focus:outline-none"
        :class="{ 'pr-16': $slots.trailing }"
      />

      <!-- Only the password field fills this, with its show/hide button. -->
      <div v-if="$slots.trailing" class="absolute top-1/2 right-2 -translate-y-1/2">
        <slot name="trailing" />
      </div>
    </div>

    <p v-if="$slots.hint" :id="hintId" class="mt-1 text-xs text-slate-500">
      <slot name="hint" />
    </p>
  </div>
</template>
