import { test, expect } from '@playwright/test';

// The app targets Tauri v2 (`@tauri-apps/api` ^2.11.1), whose `invoke()`
// (see node_modules/@tauri-apps/api/core.js) delegates to
// `window.__TAURI_INTERNALS__.invoke(cmd, args, options)` — not the legacy
// `window.__TAURI_IPC__` hook from Tauri v1. We mock at that boundary so the
// real `ladderStudioApi` wrapper (`ui/src/api/ladderStudio.ts`) round-trips
// through unmodified production code.
test('worker can click a routine, see it injected, and view the Code output', async ({ page }) => {
  await page.addInitScript(() => {
    (window as any).__TAURI_INTERNALS__ = {
      invoke: (cmd: string, _args: unknown) => {
        if (cmd === 'list_routines') {
          return Promise.resolve([{ name: 'BIT RESET', description: 'Resets the target bit' }]);
        }
        if (cmd === 'inject_routine') {
          return Promise.resolve({
            renderedAsm: 'MOV DPTR,#RLY512_519+',
            origin: { routineName: 'BIT RESET', description: 'Resets the target bit', values: {} },
          });
        }
        if (cmd === 'generate') {
          return Promise.resolve('MOV DPTR,#RLY512_519+');
        }
        return Promise.resolve(null);
      },
    };
  });

  await page.goto('/');

  // Routine appears in the palette.
  await expect(page.getByText('BIT RESET')).toBeVisible();

  // Plain click triggers the injection flow (App.tsx's handleRoutineSelect ->
  // handleRoutineDrop -> finishInjection, wired in Task 13).
  await page.getByText('BIT RESET').click();

  // The injected block renders on the canvas with the routine's description
  // (BlockNode.tsx). Scoped to `.canvas` because the same description text
  // also legitimately appears in the palette's routine list and in the
  // Plain English preview — scoping avoids a Playwright strict-mode
  // ambiguous-match failure, not a production duplication bug.
  await expect(page.locator('.canvas').getByText('Resets the target bit')).toBeVisible();

  // Switching to the Code view shows the mocked rendered ASM.
  await page.getByRole('button', { name: 'Code' }).click();
  await expect(page.getByText('MOV DPTR,#RLY512_519+')).toBeVisible();
});
