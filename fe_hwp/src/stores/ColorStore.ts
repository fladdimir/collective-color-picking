import type {
  ColorBoardInMessage,
  PositionColor,
} from "@/services/ColorWsService";
import type { RgbColor } from "@/util/ColorConverter";
import { defineStore } from "pinia";

export type OnColorChangeHandler = (newColorValue: RgbColor) => void;

export interface ColorState {
  colorBoard: ColorBoardInMessage | undefined;
  onColorChangeHandlers: OnColorChangeHandler[];
}

export const ColorStore = defineStore({
  id: "color",
  state: () =>
    ({
      colorBoard: undefined as ColorBoardInMessage | undefined,
      onColorChangeHandlers: [] as OnColorChangeHandler[],
    } as ColorState),
  getters: {
    getOwnPositionColor(): PositionColor | undefined {
      if (!this.colorBoard) return;
      return this.colorBoard.positionColors.filter(
        (pc) => pc.id === this.colorBoard?.id
      )[0];
    },
    getOwnColor(): RgbColor | undefined {
      return this.getOwnPositionColor?.color;
    },
  },
  actions: {
    setAndSyncOwnColor(color: RgbColor) {
      if (!this.colorBoard) return;
      const ownPosColor = this.getOwnPositionColor;
      if (!ownPosColor) throw new Error();
      ownPosColor.color = color;
      this.onColorChangeHandlers.forEach((cb) => cb(color));
    },
  },
});
