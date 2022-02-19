import { describe, expect, it } from "vitest";
import {
  toHsl,
  toRgb,
  type HslColor,
  type RgbColor,
} from "../../util/ColorConverter";

describe("Converter", () => {
  interface ColorPair {
    rgb: RgbColor;
    hsl: HslColor;
  }

  const testData: ColorPair[] = [
    {
      hsl: { h: 0, s: 100, l: 50 },
      rgb: { r: 255, g: 0, b: 0 },
    },
    {
      hsl: { h: 120, s: 100, l: 50 },
      rgb: { r: 0, g: 255, b: 0 },
    },
    {
      hsl: { h: 240, s: 100, l: 50 },
      rgb: { r: 0, g: 0, b: 255 },
    },
  ];

  it.each(testData)(
    "should convert hsl to rgb correctly",
    (data: ColorPair) => {
      const rgb1: RgbColor = toRgb(data.hsl);
      expect(rgb1).toEqual(data.rgb);

      const hsl1: HslColor = toHsl(rgb1);
      expect(hsl1).toEqual(data.hsl);
    }
  );
});
