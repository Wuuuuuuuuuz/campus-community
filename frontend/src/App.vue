<template>
  <div id="app">
    <NavBar />
    <main class="main-content">
      <router-view />
    </main>
    <AiChatFloat />
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import AiChatFloat from './components/AiChatFloat.vue'
</script>
