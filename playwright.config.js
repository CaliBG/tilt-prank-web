import { defineConfig } from '@playwright/test';
export default defineConfig({
 testDir: './tests', testMatch: '*.spec.js', workers: 1,
 use: {baseURL:'http://127.0.0.1:4173', browserName:'chromium', launchOptions:{executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'}, viewport:{width:393,height:851}, deviceScaleFactor:1, hasTouch:true},
 webServer:{command:'npm start',url:'http://127.0.0.1:4173',reuseExistingServer:true},
 reporter:'list'
});
