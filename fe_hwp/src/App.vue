<script setup lang="ts">
import BoardColorPicker from "./components/BoardColorPicker.vue";
import ColorCanvas from "./components/ColorCanvas.vue";
import { setupDataConnection } from "./services/ColorMessageService";

setupDataConnection();
</script>

<template>
  <ColorCanvas style="width: 100%; height: 100%; margin: 0" />
  <BoardColorPicker style="position: absolute; top: 1rem; left: 1rem" />
</template>

<style>
@import "./assets/base.css";
</style>
