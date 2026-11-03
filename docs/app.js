import { fromOrientation, formatAngle } from './math.js';
const $ = (id) => document.getElementById(id);
let mode = 'idle', timer, lastReading = null, attempt = 0;
const status = (text) => { $('status').textContent = text; };

function render(reading) {
  lastReading = reading;
  const value = reading?.displayed;
  $('angle').textContent = formatAngle(value);
  const level = value === 0;
  $('instrument').dataset.state = reading ? (level ? 'level' : 'tilted') : 'idle';
  $('verdict').textContent = !reading ? '等你来歪' : level ? '这很水平。' : value <= 10 ? '有一点，但不多。' : value <= 35 ? '这回真有点歪。' : '再歪就躺下了。';
  $('quip').textContent = !reading ? '仪器已就位，态度很随意。' : level ? '本所认为：问题不大。' : value <= 10 ? '给生活留一点误差。' : value <= 35 ? '本所已经尽力包容了。' : '这个角度，本所也圆不回来了。';
  $('stamp').innerHTML = `本所<br><strong>${!reading ? '待鉴定' : level ? '已认可' : '尽力了'}</strong>`;
  const travel = $('dial').clientWidth * .32;
  const distance = reading ? Math.min(value / 60, 1) * travel : 0;
  $('dial').style.setProperty('--bubble-x', `${reading ? reading.x * distance : 0}px`);
  $('dial').style.setProperty('--bubble-y', `${reading ? reading.y * distance : 0}px`);
}

function detach() {
  clearTimeout(timer);
  window.removeEventListener('deviceorientation', receive);
}
function stateLabel(label, sensor) {
  $('mode-tag').textContent = label;
  $('sensor-state').textContent = sensor;
}
function fail(message) {
  detach(); attempt++; mode = 'error'; render(null);
  $('start-button').disabled = false;
  $('start-label').textContent = '重新试试';
  stateLabel('暂未连接', '没有有效传感器数据');
  status(message);
}
function armTimeout() {
  clearTimeout(timer);
  timer = setTimeout(() => fail('暂时收不到动作感应。请检查浏览器权限，或点击“先试玩一下”。'), 6000);
}
function receive(event) {
  if (mode !== 'waiting' && mode !== 'live') return;
  const reading = fromOrientation(event.beta, event.gamma, screen.orientation?.angle ?? window.orientation ?? 0);
  if (!reading) return;
  mode = 'live';
  render(reading);
  $('start-button').disabled = false;
  $('start-label').textContent = '暂停测量';
  stateLabel('手机感应中', '动作传感器已连接');
  status('慢慢倾斜手机，看看它什么时候才肯承认。');
  // Some browsers emit only when orientation changes. A stationary phone
  // must retain its reading rather than being treated as disconnected.
  clearTimeout(timer);
}
async function start() {
  if (mode === 'live' || mode === 'waiting') {
    detach(); attempt++; mode = 'paused'; render(null);
    $('start-label').textContent = '继续测量';
    stateLabel('已暂停', '传感器已停止'); status('休息一下。点击“继续测量”重新开始。'); return;
  }
  detach();
  const token = ++attempt;
  mode = 'waiting'; render(null);
  $('demo-controls').hidden = true;
  $('demo-button').textContent = '先试玩一下';
  $('start-button').disabled = true;
  $('start-label').textContent = '连接中…';
  stateLabel('连接中', '等待动作传感器');
  status('请允许浏览器访问动作与方向，然后轻轻倾斜手机。');
  if (!window.isSecureContext) { fail('动作感应需要安全连接，请使用 HTTPS 网址打开。'); return; }
  if (!('DeviceOrientationEvent' in window)) { fail('这个浏览器无法读取手机倾斜。请用手机浏览器打开，或先试玩一下。'); return; }
  try {
    if (typeof DeviceOrientationEvent.requestPermission === 'function') {
      const permission = await DeviceOrientationEvent.requestPermission();
      if (token !== attempt) return;
      if (permission !== 'granted') { fail('动作感应权限未开启。请在浏览器设置中允许访问后重试，也可以先试玩。'); return; }
    }
    if (token !== attempt) return;
    window.addEventListener('deviceorientation', receive);
    armTimeout();
  } catch {
    if (token === attempt) fail('无法启动动作感应。请检查浏览器权限，或先试玩一下。');
  }
}
function updateDemo() {
  const actual = Number($('demo-angle').value);
  $('actual-angle').value = `${actual}°`;
  render(fromOrientation(actual, 0));
  document.querySelectorAll('[data-angle]').forEach(button => button.setAttribute('aria-pressed', String(Number(button.dataset.angle) === actual)));
}
function demo() {
  detach(); attempt++; mode = 'demo';
  $('start-button').disabled = false;
  $('start-label').textContent = '开始测量';
  $('demo-button').textContent = '正在试玩';
  $('demo-controls').hidden = false;
  stateLabel('试玩模式', '模拟输入 · 非手机读数');
  status('拖动滑杆，或者选一个角度。看看本所怎么解释。');
  updateDemo();
}
$('start-button').addEventListener('click', start);
$('demo-button').addEventListener('click', demo);
$('demo-angle').addEventListener('input', updateDemo);
document.querySelectorAll('[data-angle]').forEach(button => button.addEventListener('click', () => {
  $('demo-angle').value = button.dataset.angle; updateDemo();
}));
window.addEventListener('resize', () => render(lastReading));
document.addEventListener('visibilitychange', () => {
  if (document.hidden && ['live', 'waiting'].includes(mode)) {
    detach(); attempt++; mode = 'paused'; render(null);
    $('start-button').disabled = false; $('start-label').textContent = '继续测量';
    stateLabel('已暂停', '传感器已停止'); status('已暂停。回到页面后点击“继续测量”。');
  }
});
window.addEventListener('pagehide', () => { detach(); attempt++; });
for (const [open, close, dialog] of [['about-button', 'close-about', 'about-dialog'], ['share-button', 'close-share', 'share-dialog']]) {
  $(open).addEventListener('click', () => {
    if (dialog === 'share-dialog') { $('share-url').value = location.href.split('#')[0]; $('copy-status').textContent = ''; }
    $(dialog).showModal();
  });
  $(close).addEventListener('click', () => $(dialog).close());
  $(dialog).addEventListener('click', event => { if (event.target === $(dialog)) {
    const r = $(dialog).getBoundingClientRect();
    if (event.clientX < r.left || event.clientX > r.right || event.clientY < r.top || event.clientY > r.bottom) $(dialog).close();
  }});
}
$('copy-button').addEventListener('click', async () => {
  try { await navigator.clipboard.writeText($('share-url').value); $('copy-status').textContent = '已复制，发给朋友吧。'; }
  catch { $('share-url').focus(); $('share-url').select(); $('copy-status').textContent = '请长按选中的网址，手动复制。'; }
});
render(null);
