import { chromium } from '@playwright/test';
import { readFile } from 'node:fs/promises';
const browser=await chromium.launch({executablePath:process.env.CHROME_PATH||'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
try {
 const page=await browser.newPage({viewport:{width:1024,height:1024},deviceScaleFactor:1});
 await page.setContent(`<style>html,body{margin:0;width:1024px;height:1024px;overflow:hidden}svg{display:block}</style>${await readFile('docs/assets/level-icon.svg','utf8')}`);
 await page.screenshot({path:'docs/assets/icon.png',omitBackground:false});
} finally {await browser.close();}
