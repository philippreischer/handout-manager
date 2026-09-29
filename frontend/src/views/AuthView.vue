<script setup lang="ts">
// Shared frame for the auth pages. Stays mounted while the child route
// switches between login and register.
</script>

<template>
  <!-- 3.5rem is the navbar height (h-14), so the card centers in what is left. -->
  <div class="flex min-h-[calc(100vh-3.5rem)] items-center justify-center p-4">
    <div class="w-full max-w-md rounded-xl bg-slate-800 p-8 shadow-xl">
      <h1 class="mb-8 text-center text-2xl font-bold text-slate-100">
        handout-manager
      </h1>

      <RouterView />
    </div>
  </div>
</template>