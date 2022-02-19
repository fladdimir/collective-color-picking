<script setup lang="ts">
import { ColorStore } from "@/stores/ColorStore";
import type { RgbColor } from "@/util/ColorConverter";
import { onMounted } from "vue";

const store = ColorStore();

let canvas: HTMLCanvasElement;
let ctx: CanvasRenderingContext2D;

const circle = (x: number, y: number, r: number, c: RgbColor) => {
  ctx.beginPath();
  var rad = ctx.createRadialGradient(x, y, 1, x, y, r);
  rad.addColorStop(0, rgbToString(c, 0.6));
  rad.addColorStop(0.2, rgbToString(c, 0.4));
  rad.addColorStop(1, rgbToString(c, 0));
  ctx.fillStyle = rad;
  ctx.arc(x, y, r, 0, Math.PI * 2, false);
  ctx.fill();
};

const clear = () => ctx.clearRect(0, 0, canvas.width, canvas.height);

const MAX_SCALE = 100;
const CIRCLE_RADIUS_FACTOR = 0.8;

const drawState = () => {
  clear();
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  if (!store.colorBoard) return;

  const pcs = store.colorBoard.positionColors;

  const positions = pcs.map((pc) => pc.position);
  const maxOffX = Math.max(
    ...positions.map((p) => p.x).map((v) => Math.abs(v))
  );
  const maxOffY = Math.max(
    ...positions.map((p) => p.y).map((v) => Math.abs(v))
  );
  const scaleX = canvas.width / 2 / (maxOffX + 1);
  const scaleY = canvas.height / 2 / (maxOffY + 1);
  const scale = Math.min(scaleX, scaleY, MAX_SCALE);
  const radius = scale * CIRCLE_RADIUS_FACTOR;
  pcs.forEach((pc) => {
    const x = canvas.width / 2 + pc.position.x * scale;
    const y = canvas.height / 2 + pc.position.y * scale;
    circle(x, y, radius, pc.color);
  });
};

onMounted(() => {
  canvas = document.getElementById("app-canvas") as HTMLCanvasElement;
  ctx = canvas.getContext("2d") as CanvasRenderingContext2D;

  ctx.globalCompositeOperation = "lighter";
  drawState();
  store.$subscribe(drawState);
  window.addEventListener("resize", drawState);
});

const rgbToString = (rgb: RgbColor, a: number) =>
  `rgba(${rgb.r},${rgb.g},${rgb.b},${a})`;
</script>

<template>
  <canvas id="app-canvas"></canvas>
</template>
