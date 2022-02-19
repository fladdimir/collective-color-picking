import { ColorStore } from "@/stores/ColorStore";
import {
  establishWsConnection,
  type ColorBoardInMessage,
} from "./ColorWsService";

export function setupDataConnection() {
  const msgSender = establishWsConnection(handleMessage);
  const store = ColorStore();
  store.onColorChangeHandlers.push((newColor) => msgSender(newColor));
}

function handleMessage(msg: ColorBoardInMessage): void {
  const store = ColorStore();
  store.colorBoard = msg;
}
