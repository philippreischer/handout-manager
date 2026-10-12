<script setup lang="ts">
import UserNameSection from '@/components/settings/UserNameSection.vue'
import PasswordSection from '@/components/settings/PasswordSection.vue'
import DeleteAccountSection from '@/components/settings/DeleteAccountSection.vue'
</script>

<template>
  
  <div class="mx-auto min-h-[calc(100vh-3.5rem)] w-full max-w-xl space-y-6 p-4">
    <h1 class="pt-4 text-2xl font-bold text-slate-100">Settings</h1>

    <UserNameSection />
    <PasswordSection />
    <DeleteAccountSection />
  </div>
</template>