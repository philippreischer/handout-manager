<script setup lang="ts">
import FormField from './FormField.vue'

const model = defineModel<string>({ required: true })

withDefaults(
  defineProps<{
    id: string
    label?: string
  }>(),
  { label: 'Email' },
)
</script>

<template>
  <FormField
    :id="id"
    :label="label"
    v-model="model"
    type="email"
    required
    maxlength="255"
    autocomplete="email"
  />
</template>
