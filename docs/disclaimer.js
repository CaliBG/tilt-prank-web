(() => {
  const storageKey = 'waili-disclaimer-v1';
  const dialog = document.getElementById('disclaimer-dialog');
  function openDisclaimer() {
    dialog.showModal();
    document.documentElement.classList.add('disclaimer-open');
  }
  document.getElementById('disclaimer-button').addEventListener('click', openDisclaimer);
  // Require an explicit acknowledgement, including on first visit.
  dialog.addEventListener('cancel', event => event.preventDefault());
  document.getElementById('accept-disclaimer').addEventListener('click', () => {
    try { localStorage.setItem(storageKey, 'accepted'); } catch { /* Storage may be unavailable. */ }
    dialog.close();
    document.documentElement.classList.remove('disclaimer-open');
  });
  let accepted = false;
  try { accepted = localStorage.getItem(storageKey) === 'accepted'; } catch { /* Still show the notice. */ }
  if (!accepted) openDisclaimer();
})();
