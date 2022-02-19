<script setup lang="ts">
import { ColorStore } from "@/stores/ColorStore";
import ColorPicker from "@radial-color-picker/vue-color-picker";
import { toHsl, toRgb, type HslColor } from "../util/ColorConverter";

const store = ColorStore();

interface CpColor {
  hue: number;
  saturation: number;
  luminosity: number;
  alpha: number;
}

function toCpColor(hslColor: HslColor): CpColor {
  const { h, s, l } = hslColor;
  return { hue: h, saturation: s, luminosity: l, alpha: 1 };
}

// alternative: @change
function onSelectedColorChange(hue: number): void {
  const ownColor = store.getOwnColor;
  if (!ownColor) return;
  const { s, l } = toHsl(ownColor);
  store.setAndSyncOwnColor(toRgb({ h: hue, s, l }));
}
</script>

<template>
  <div>
    <ColorPicker
      v-bind="toCpColor(toHsl(store.getOwnColor ?? { r: 0, g: 0, b: 0 }))"
      @input="onSelectedColorChange"
    />
  </div>
</template>

<style>
@import "@radial-color-picker/vue-color-picker/dist/vue-color-picker.min.css";
</style>
