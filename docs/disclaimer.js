(() => {
  const dialog = document.getElementById('disclaimer-dialog');
  function openDisclaimer() {
    if (dialog.open) return;
    dialog.showModal();
    dialog.querySelector('.disclaimer-body').scrollTop = 0;
    document.documentElement.classList.add('disclaimer-open');
  }
  document.getElementById('disclaimer-button').addEventListener('click', openDisclaimer);
  // Every page visit requires an explicit acknowledgement.
  dialog.addEventListener('cancel', event => event.preventDefault());
  document.getElementById('accept-disclaimer').addEventListener('click', () => {
    dialog.close();
    document.documentElement.classList.remove('disclaimer-open');
  });
  window.addEventListener('pageshow', event => {
    if (event.persisted) openDisclaimer();
  });
  openDisclaimer();
})();
