import { test, expect } from '@playwright/test';
async function visit(page) {
 await page.goto('/');
 await page.locator('#accept-disclaimer').click();
}
async function orientation(page, beta, gamma) {
 await page.evaluate(({beta,gamma}) => window.dispatchEvent(new DeviceOrientationEvent('deviceorientation',{beta,gamma})),{beta,gamma});
}
test('mobile demo boundaries, layout and screenshots', async ({page}) => {
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await visit(page);
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
 await visit(page);await page.locator('#start-button').click();
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
 await visit(page);await page.locator('#start-button').click();
 await expect(page.locator('#status')).toContainText('权限未开启');
 await expect(page.locator('#angle')).toHaveText('—');
 await expect(page.locator('#start-button')).toBeEnabled();
});
test('sensor absence times out without fake zero', async ({page}) => {
 await page.clock.install();await visit(page);await page.locator('#start-button').click();
 await page.clock.fastForward(6100);
 await expect(page.locator('#status')).toContainText('暂时收不到');
 await expect(page.locator('#angle')).toHaveText('—');
});
test('invalid initial data is ignored and stationary readings stay available', async ({page}) => {
 await page.clock.install();await visit(page);await page.locator('#start-button').click();
 await orientation(page,null,null);await expect(page.locator('#angle')).toHaveText('—');
 await orientation(page,15,0);await expect(page.locator('#angle')).toHaveText('5');
 await page.clock.fastForward(6100);await expect(page.locator('#angle')).toHaveText('5');
});
test('help and share dialogs work', async ({page}) => {
 await visit(page);await page.locator('#about-button').click();
 await expect(page.locator('#about-dialog')).toBeVisible();
 await page.locator('summary').click();await expect(page.locator('details')).toHaveAttribute('open','');
 await page.keyboard.press('Escape');await expect(page.locator('#about-dialog')).not.toBeVisible();
 await page.locator('#share-button').click();await expect(page.locator('#share-url')).toHaveValue('http://127.0.0.1:4173/');
 await page.locator('#close-share').click();await expect(page.locator('#share-dialog')).not.toBeVisible();
});
test('desktop and narrow mobile remain usable', async ({page}) => {
 await page.setViewportSize({width:1365,height:900});await visit(page);
 await page.screenshot({path:'.qa/desktop.png',fullPage:true});
 await page.setViewportSize({width:320,height:720});
 expect(await page.evaluate(()=>document.documentElement.scrollWidth <= innerWidth)).toBe(true);
 await expect(page.locator('#start-button')).toBeVisible();
 await page.screenshot({path:'.qa/narrow-mobile.png',fullPage:true});
});
test('Android APK download and iOS assisted installation notice', async ({page}) => {
 await visit(page);
 await expect(page.locator('.ios-install')).toContainText('不上架 App Store');
 await expect(page.locator('.ios-install')).toContainText('请联系我本人');
 const downloadEvent=page.waitForEvent('download');
 await page.locator('#apk-download').click();
 const download=await downloadEvent;
 expect(download.suggestedFilename()).toBe('Level-1.0.4.apk');
 expect(await download.failure()).toBeNull();
});

test('first visit requires acknowledgement, remembers it and allows reopening', async ({page}) => {
 await page.goto('/');
 const dialog = page.getByRole('dialog', {name:'免责声明'});
 await expect(dialog).toBeVisible();
 await expect(page.locator('#disclaimer-title')).toBeFocused();
 await expect(dialog).toContainText('超过 10° 后显示实际角度减 10°');
 await page.keyboard.press('Escape');
 await expect(dialog).toBeVisible();
 expect(await page.locator('#demo-button').evaluate(el => el.closest('body').querySelector(':modal').id)).toBe('disclaimer-dialog');
 await page.locator('#accept-disclaimer').click();
 await expect(dialog).not.toBeVisible();
 await page.reload();
 await expect(dialog).not.toBeVisible();
 await page.locator('#disclaimer-button').click();
 await expect(dialog).toBeVisible();
 await page.locator('#accept-disclaimer').click();
 await page.locator('#demo-button').click();
 await expect(page.locator('#angle')).toHaveText('5');
});
test('disclaimer fits narrow screens and remains usable without storage', async ({page}) => {
 await page.addInitScript(() => {
   Storage.prototype.getItem = () => { throw new Error('storage disabled'); };
   Storage.prototype.setItem = () => { throw new Error('storage disabled'); };
 });
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.setViewportSize({width:320,height:568});
 await page.goto('/');
 await expect(page.locator('#disclaimer-dialog')).toBeVisible();
 expect(await page.locator('#disclaimer-dialog').evaluate(el => {
   const r=el.getBoundingClientRect();
   return r.left>=0 && r.right<=innerWidth && r.top>=0 && r.bottom<=innerHeight && el.scrollWidth<=el.clientWidth;
 })).toBe(true);
 await page.screenshot({path:'.qa/disclaimer-mobile.png'});
 await page.locator('#accept-disclaimer').click();
 await expect(page.locator('#disclaimer-dialog')).not.toBeVisible();
 await page.locator('#demo-button').click();
 await expect(page.locator('#angle')).toHaveText('5');
 await page.reload();
 await expect(page.locator('#disclaimer-dialog')).toBeVisible();
 expect(errors).toEqual([]);
 await page.setViewportSize({width:1280,height:900});
 await page.screenshot({path:'.qa/disclaimer-desktop.png'});
});
