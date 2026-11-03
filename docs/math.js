const RAD = Math.PI / 180;

export function displayAngle(actual) {
  if (!Number.isFinite(actual)) return null;
  const magnitude = Math.abs(actual);
  return magnitude <= 10 + 1e-10 ? 0 : Math.sign(actual) * (magnitude - 10);
}

export function formatAngle(angle) {
  if (!Number.isFinite(angle)) return '—';
  if (angle === 0) return '0';
  const rounded = Math.max(0.1, Math.round(Math.abs(angle) * 10) / 10);
  return `${angle < 0 ? '-' : ''}${rounded}`;
}

// W3C Z-X-Y rotation: the final matrix row is the world vertical in device axes.
export function fromOrientation(beta, gamma, screenAngle = 0) {
  if (![beta, gamma, screenAngle].every(Number.isFinite)) return null;
  if (Math.abs(beta) > 180 || Math.abs(gamma) > 90) return null;
  const b = beta * RAD, g = gamma * RAD;
  const x = -Math.cos(b) * Math.sin(g);
  const y = Math.sin(b);
  const z = Math.cos(b) * Math.cos(g);
  const length = Math.hypot(x, y);
  const actual = Math.atan2(length, Math.abs(z)) / RAD;
  const displayed = displayAngle(actual);
  const s = screenAngle * RAD;
  return {
    actual, displayed,
    x: length < 1e-10 ? 0 : (x * Math.cos(s) + y * Math.sin(s)) / length,
    y: length < 1e-10 ? 0 : (x * Math.sin(s) - y * Math.cos(s)) / length
  };
}
