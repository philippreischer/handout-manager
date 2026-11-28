<script setup lang="ts">
import FormTextArea from './FormTextArea.vue'

const model = defineModel<string>({ required: true })

withDefaults(
  defineProps<{
    id: string
    label?: string
  }>(),
  { label: 'Description' },
)
</script>

<template>
  <FormTextArea
    :id="id"
    :label="label"
    v-model="model"
    maxlength="2000"
    autocomplete="off"
  />
</template>