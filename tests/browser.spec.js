import { test, expect } from '@playwright/test';
async function orientation(page, beta, gamma) {
 await page.evaluate(({beta,gamma}) => window.dispatchEvent(new DeviceOrientationEvent('deviceorientation',{beta,gamma})),{beta,gamma});
}
test('mobile demo boundaries, layout and screenshots', async ({page}) => {
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.goto('/');
 await expect(page.locator('#angle')).toHaveText('—');
 await expect(page.locator('#instrument')).toHaveAttribute('data-state','idle');
 await page.getByRole('button',{name:'先试玩一下'}).click();
 await expect(page.locator('#angle')).toHaveText('5');
 for(const [actual,shown,state] of [[10,'0','level'],[20,'10','tilted'],[0,'0','level'],[45,'35','tilted']]) {
   await page.locator(`[data-angle="${actual}"]`).click();
   await expect(page.locator('#angle')).toHaveText(shown);
   await expect(page.locator('#instrument')).toHaveAttribute('data-state',state);
 }
 await page.locator('[data-angle="10"]').click();
 await page.waitForTimeout(300);
 await page.screenshot({path:'.qa/mobile-level.png',fullPage:true});
 await page.locator('[data-angle="15"]').click();
 await page.waitForTimeout(300);
 await page.screenshot({path:'.qa/mobile-tilted.png',fullPage:true});
 expect(await page.evaluate(()=>document.documentElement.scrollWidth <= innerWidth)).toBe(true);
 expect(errors).toEqual([]);
});
test('live sensor path, pause and demo switching', async ({page}) => {
 await page.goto('/');await page.locator('#start-button').click();
 await orientation(page,10,0);await expect(page.locator('#angle')).toHaveText('0');
 await orientation(page,15,0);await expect(page.locator('#angle')).toHaveText('5');
 await orientation(page,0,20);await expect(page.locator('#angle')).toHaveText('10');
 await expect(page.locator('#mode-tag')).toHaveText('手机感应中');
 await page.locator('#start-button').click();await expect(page.locator('#angle')).toHaveText('—');
 await orientation(page,15,0);await expect(page.locator('#angle')).toHaveText('—');
 await page.locator('#demo-button').click();
 await orientation(page,80,0);await expect(page.locator('#angle')).toHaveText('5');
});
test('denied sensor permission is explicit', async ({page}) => {
 await page.addInitScript(()=> { DeviceOrientationEvent.requestPermission = async()=> 'denied'; });
 await page.goto('/');await page.locator('#start-button').click();
 await expect(page.locator('#status')).toContainText('权限未开启');
 await expect(page.locator('#angle')).toHaveText('—');
 await expect(page.locator('#start-button')).toBeEnabled();
});
test('sensor absence times out without fake zero', async ({page}) => {
 await page.clock.install();await page.goto('/');await page.locator('#start-button').click();
 await page.clock.fastForward(6100);
 await expect(page.locator('#status')).toContainText('暂时收不到');
 await expect(page.locator('#angle')).toHaveText('—');
});
test('invalid initial data is ignored and stationary readings stay available', async ({page}) => {
 await page.clock.install();await page.goto('/');await page.locator('#start-button').click();
 await orientation(page,null,null);await expect(page.locator('#angle')).toHaveText('—');
 await orientation(page,15,0);await expect(page.locator('#angle')).toHaveText('5');
 await page.clock.fastForward(6100);await expect(page.locator('#angle')).toHaveText('5');
});
test('help and share dialogs work', async ({page}) => {
 await page.goto('/');await page.locator('#about-button').click();
 await expect(page.locator('#about-dialog')).toBeVisible();
 await page.locator('summary').click();await expect(page.locator('details')).toHaveAttribute('open','');
 await page.keyboard.press('Escape');await expect(page.locator('#about-dialog')).not.toBeVisible();
 await page.locator('#share-button').click();await expect(page.locator('#share-url')).toHaveValue('http://127.0.0.1:4173/');
 await page.locator('#close-share').click();await expect(page.locator('#share-dialog')).not.toBeVisible();
});
test('desktop and narrow mobile remain usable', async ({page}) => {
 await page.setViewportSize({width:1365,height:900});await page.goto('/');
 await page.screenshot({path:'.qa/desktop.png',fullPage:true});
 await page.setViewportSize({width:320,height:720});
 expect(await page.evaluate(()=>document.documentElement.scrollWidth <= innerWidth)).toBe(true);
 await expect(page.locator('#start-button')).toBeVisible();
 await page.screenshot({path:'.qa/narrow-mobile.png',fullPage:true});
});
