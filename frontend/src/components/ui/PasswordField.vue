<script setup lang="ts">
import { computed, ref } from 'vue'
import FormField from './FormField.vue'

const model = defineModel<string>({ required: true })
const showPassword = ref(false)

const props = withDefaults(
  defineProps<{
    id: string
    label?: string
    purpose?: 'current' | 'new'
  }>(),
  { label: 'Password', purpose: 'current' },
)

const isNew = computed(() => props.purpose === 'new')
</script>

<template>
  <FormField
    :id="id"
    :label="label"
    v-model="model"
    :type="showPassword ? 'text' : 'password'"
    required
    :minlength="isNew ? 8 : undefined"
    maxlength="72"
    :autocomplete="isNew ? 'new-password' : 'current-password'"
  >
    <template #trailing>
      <button
        type="button"
        class="text-xs text-slate-400 hover:text-slate-200"
        @click="showPassword = !showPassword"
      >
        {{ showPassword ? 'Hide' : 'Show' }}
      </button>
    </template>

    <template v-if="isNew" #hint>At least 8 characters.</template>
  </FormField>
</template>
