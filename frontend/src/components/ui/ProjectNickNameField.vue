<script setup lang="ts">
import FormField from './FormField.vue'

const model = defineModel<string>({ required: true })

withDefaults(
  defineProps<{
    id: string
    label?: string
  }>(),
  { label: 'NickName' },
)
</script>

<template>
  <FormField
    :id="id"
    :label="label"
    v-model="model"
    maxlength="255"
    autocomplete="off"
  />
</template>