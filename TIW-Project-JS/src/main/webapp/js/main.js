import { setupPageVendo, freePageVendo } from './vendo.js';
import { setupPageAscquisto, freePageAcquisto } from './acquisto.js';
import { loadDettaglioAsta } from './dettaglioAsta.js';
import { renderOffertaPage, hideAllPages } from './offerte.js';

// Navigation elements
document.addEventListener('DOMContentLoaded', () => {
  const moveToVendo = document.getElementById('moveToVendo');
  const moveToAcquisto = document.getElementById('moveToAcquisto');

  moveToVendo.addEventListener('click', () => {
    showVendo();
  });
  moveToAcquisto.addEventListener('click', () => {
    showAcquisto();
  });

  // Initial view based on last action
  const last = localStorage.getItem('lastAction');
  if (last === 'addedAsta') {
    moveToAcquisto.removeAttribute('hidden');
    moveToVendo.setAttribute('hidden', true);
    showVendo();
  } else {
    moveToVendo.removeAttribute('hidden');
    moveToAcquisto.setAttribute('hidden', true);
    showAcquisto();
  }

});

// Show "Vendo" page
function showVendo() {
  hideAllPages();
  //freePageAcquisto();
  setupPageVendo();
}

// Show "Acquisto" page
function showAcquisto() {
  hideAllPages();
  //freePageVendo();
  setupPageAscquisto();
  // Optionally, render previously visited auctions if needed
}


// Save visited auctions in client storage (1 month)
function saveVisited(idAsta) {
  const key = 'asteLastVisited';
  const stored = localStorage.getItem(key);
  const visits = stored ? JSON.parse(stored) : [];
  if (!visits.includes(idAsta)) {
    visits.push(idAsta);
    localStorage.setItem(key, JSON.stringify(visits));
  }
}
