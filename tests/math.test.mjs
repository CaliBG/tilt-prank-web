import test from 'node:test';
import assert from 'node:assert/strict';
import { displayAngle, formatAngle, fromOrientation } from '../docs/math.js';
const near = (a,b) => assert.ok(Math.abs(a-b) < 1e-8, `${a} should equal ${b}`);
test('inclusive dead zone and continuous mapping', () => {
  for (const a of [-10,-5,0,5,10]) assert.equal(displayAngle(a),0);
  for (const [a,b] of [[15,5],[20,10],[45,35],[90,80],[-15,-5]]) near(displayAngle(a),b);
  near(displayAngle(10.001),.001);
});
test('small nonzero readings never format as zero', () => {
  assert.equal(formatAngle(0),'0'); assert.equal(formatAngle(.001),'0.1');
  assert.equal(formatAngle(5),'5'); assert.equal(formatAngle(5.16),'5.2');
  assert.equal(formatAngle(null),'—');
});
test('front, back, left, right all agree', () => {
  for(const [b,g] of [[15,0],[-15,0],[0,15],[0,-15]]) near(fromOrientation(b,g).displayed,5);
});
test('diagonal tilt is combined, not independently clamped', () => {
  const r = fromOrientation(9,9); assert.ok(r.actual > 12 && r.actual < 13); assert.ok(r.displayed > 2);
});
test('face down and face up are horizontal, upright is 80', () => {
  near(fromOrientation(0,0).displayed,0); near(fromOrientation(180,0).displayed,0);
  near(fromOrientation(90,45).displayed,80); near(fromOrientation(0,90).displayed,80);
});
test('10 degree boundary in either axis is zero', () => {
  for(const [b,g] of [[10,0],[-10,0],[0,10],[0,-10]]) assert.equal(fromOrientation(b,g).displayed,0);
});
test('screen rotation changes direction but not angle', () => {
  const p = fromOrientation(15,0,0), l = fromOrientation(15,0,90);
  near(p.displayed,l.displayed); near(p.x,0); near(p.y,-1); near(l.x,1); near(l.y,0);
});
test('missing and invalid readings do not turn into zero', () => {
  for(const bad of [null,undefined,NaN,Infinity,'0']) {
    assert.equal(fromOrientation(bad,0),null); assert.equal(fromOrientation(0,bad),null);
  }
  assert.equal(fromOrientation(181,0),null); assert.equal(fromOrientation(0,91),null);
});
