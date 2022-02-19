export interface RgbColor {
  r: number;
  g: number;
  b: number;
}

export interface HslColor {
  h: number;
  s: number;
  l: number;
}

const MIN_RGB = 0;
const MAX_RGB = 255;
const FIX_S_L = true;

export function toHsl(rgbColor: RgbColor): HslColor {
  let { r, g, b } = rgbColor;
  if (r < MIN_RGB) r = MIN_RGB;
  if (g < MIN_RGB) g = MIN_RGB;
  if (b < MIN_RGB) b = MIN_RGB;
  if (r > MAX_RGB) r = MAX_RGB;
  if (g > MAX_RGB) g = MAX_RGB;
  if (b > MAX_RGB) b = MAX_RGB;
  r /= MAX_RGB;
  g /= MAX_RGB;
  b /= MAX_RGB;
  const M = Math.max(r, g, b);
  const m = Math.min(r, g, b);
  const d = M - m;
  let h, l, s;
  if (d == 0) h = 0;
  else if (M == r) h = ((g - b) / d) % 6;
  else if (M == g) h = (b - r) / d + 2;
  else h = (r - g) / d + 4;
  h *= 60;
  if (h < 0) h += 360;
  l = (M + m) / 2;
  if (d == 0) s = 0;
  else s = d / (1 - Math.abs(2 * l - 1));
  s *= 100;
  l *= 100;
  h = Math.round(h);
  s = Math.round(s);
  l = Math.round(l);
  if (FIX_S_L) {
    s = 100;
    l = 50;
  }
  return { h, s, l };
}

export function toRgb(hsl: HslColor): RgbColor {
  let { h, s, l } = hsl;
  if (h < 0) h = 0;
  if (s < 0) s = 0;
  if (l < 0) l = 0;
  if (h >= 360) h = 359;
  if (s > 100) s = 100;
  if (l > 100) l = 100;
  s /= 100;
  l /= 100;
  const C = (1 - Math.abs(2 * l - 1)) * s;
  const hh = h / 60;
  const X = C * (1 - Math.abs((hh % 2) - 1));
  let r = 0,
    g = 0,
    b = 0;
  if (hh >= 0 && hh < 1) {
    r = C;
    g = X;
  } else if (hh >= 1 && hh < 2) {
    r = X;
    g = C;
  } else if (hh >= 2 && hh < 3) {
    g = C;
    b = X;
  } else if (hh >= 3 && hh < 4) {
    g = X;
    b = C;
  } else if (hh >= 4 && hh < 5) {
    r = X;
    b = C;
  } else {
    r = C;
    b = X;
  }
  const m = l - C / 2;
  r += m;
  g += m;
  b += m;
  r *= MAX_RGB;
  g *= MAX_RGB;
  b *= MAX_RGB;
  r = Math.round(r);
  g = Math.round(g);
  b = Math.round(b);
  return { r, g, b };
}
