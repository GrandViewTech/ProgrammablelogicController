import { invoke } from '@tauri-apps/api/core';

export type Screen = {
  rows: RowScreen[];
  endRowNumber: number | null;
  endColumnNumber: number | null;
};

export type RowScreen = { rowNumber: number; columns: ColumnScreen[]; outputName?: string | null };

export type Combinator = 'AND' | 'OR' | 'XOR';

export type RoutineOrigin = {
  routineName: string;
  description: string;
  values: Record<number, string>;
};

export type ColumnScreen = {
  rowNumber: number;
  columnNumber: number;
  coilType: string;
  inputType: string | null;
  value: string;
  tag: string;
  comment: string;
  routineOrigin: RoutineOrigin | null;
  renderedAsm: string | null;
  combinator: Combinator | null;
  isBlank: boolean;
  inverted?: boolean;
  group?: ColumnScreen[];
  rowRefName?: string | null;
};

export type RoutineSummary = { name: string; description: string };
export type InjectedBlock = { renderedAsm: string; origin: RoutineOrigin };

export const ladderStudioApi = {
  generate: (screen: Screen) => invoke<string>('generate', { screen }),
  listRoutines: () => invoke<RoutineSummary[]>('list_routines'),
  injectRoutine: (routineName: string, values: Record<number, string>) =>
    invoke<InjectedBlock>('inject_routine', { routineName, values }),
  saveScreen: (name: string, screen: Screen) => invoke<void>('save_screen', { name, screen }),
  loadScreen: (name: string) => invoke<Screen>('load_screen', { name }),
};
