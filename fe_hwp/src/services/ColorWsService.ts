export interface ColorBoardInMessage {
  id: string;
  positionColors: PositionColor[];
}

export interface PositionColor {
  id: string;
  position: Position;
  color: Color;
}

export interface Color {
  r: number;
  g: number;
  b: number;
}

export interface Position {
  x: number;
  y: number;
}

type ColorBoardInMessageHandler = (arg: ColorBoardInMessage) => void;
type ColorBoardOutMessageSender = (arg: Color) => void;

let messageCallback: ColorBoardInMessageHandler;

function onopen(event: Event): void {
  console.log(event);
}

function onmessage(event: MessageEvent): void {
  const message = JSON.parse(event.data);
  messageCallback(message);
}

function onclose(event: CloseEvent): void {
  console.log(event);
}

function onerror(event: Event): void {
  console.error(event);
  alert("websocket connection error");
}

function sendOutMessage(connection: WebSocket, color: Color): void {
  connection.send(JSON.stringify(color));
}

export function establishWsConnection(
  msgHandler: ColorBoardInMessageHandler
): ColorBoardOutMessageSender {
  console.log("trying to open websocket...");
  const useSsl = location.protocol.includes("https");
  const connection = new WebSocket(
    (useSsl ? "wss" : "ws") + "://" + location.host + "/api/colorboardws"
  );
  connection.onopen = onopen;
  connection.onmessage = onmessage;
  connection.onclose = onclose;
  connection.onerror = onerror;
  messageCallback = msgHandler;
  return (color: Color) => sendOutMessage(connection, color);
}
