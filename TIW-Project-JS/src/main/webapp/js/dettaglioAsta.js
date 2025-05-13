import { hideAllPages} from './main.js';

// Carica e renderizza i dettagli di un'asta
export function renderDettaglioAstaPage(idAsta) {
  hideAllPages();
  const page = document.getElementById('dettaglioAstaPage');

  // Pulisce campi dinamici in entrambe le sezioni
  ['creatore','prezzo','rialzo','dataScadenza','oraScadenza']
    .forEach(field => {
      const el = page.querySelector(`#astaAperta #${field}`);
      if (el) el.textContent = '';
    });
  ['creatore','prezzoIniziale','rialzoMinimo','dataScadenza','oraScadenza',
   'prezzo','nomeAcquirente','indirizzo']
    .forEach(field => {
      const el = page.querySelector(`#astaChiusa #${field}`);
      if (el) el.textContent = '';
    });

  // Pulisce la tabella offerte
  const offTable = document.getElementById('listaOfferte');
  if (offTable) offTable.innerHTML = '';

  // Richiesta GET alla servlet
  const xhr = new XMLHttpRequest();
  xhr.open('GET', `/TIW-Project/DettaglioAstaPageServlet?idAsta=${encodeURIComponent(idAsta)}`, true);
  xhr.responseType = 'json';

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert(`Errore ${xhr.status} nel caricamento della pagina`);
      return;
    }
    const result = xhr.response;
    const openSec = page.querySelector('#astaAperta');
    const closedSec = page.querySelector('#astaChiusa');

    if (result.openAsta) {
      // Mostra sezione asta aperta
      openSec.hidden = false;
      closedSec.hidden = true;
      const { creatore, prezzoIniziale, rialzoMinimo, dataScadenza, oraScadenza } = result.openAsta;

      openSec.querySelector('#creatore').textContent = creatore;
      openSec.querySelector('#prezzo').textContent = prezzoIniziale.toFixed(2);
      openSec.querySelector('#rialzo').textContent = rialzoMinimo.toFixed(2);
      openSec.querySelector('#dataScadenza').textContent = dataScadenza;
      openSec.querySelector('#oraScadenza').textContent = oraScadenza;

      // Popola tabella offerte (tbody)
      const tbody = openSec.querySelector('table tbody');
      result.offerte.forEach(o => {
        const row = tbody.insertRow();
        row.insertCell().textContent = o.username;
        row.insertCell().textContent = o.prezzo.toFixed(2);
        row.insertCell().textContent = o.dataOfferta;
        row.insertCell().textContent = o.oraOfferta;
      });

      // Gestione bottone "Chiudi Asta"
      const canDiv = page.getElementById('canBeClosed');
      const btn = canDiv.querySelector('button');
      if (result.canBeClosed) {
        canDiv.hidden = false;


        btn.addEventListener('click', e => {
          e.preventDefault();
          handlerCloseAsta(idAsta);
          btn.removeEventListener('click', handlerCloseAsta);
        });
      } else {
        canDiv.hidden = true;
      }

    } else {
      // Mostra sezione asta chiusa
      openSec.hidden  = true;
      closedSec.hidden = false;
      const ac = result.astaChiusa;

      closedSec.querySelector('#creatore').textContent = ac.creatore;
      closedSec.querySelector('#prezzoIniziale').textContent = ac.prezzoIniziale.toFixed(2);
      closedSec.querySelector('#rialzoMinimo').textContent = ac.rialzoMinimo.toFixed(2);
      closedSec.querySelector('#dataScadenza').textContent = ac.dataScadenza;
      closedSec.querySelector('#oraScadenza').textContent = ac.oraScadenza;
      closedSec.querySelector('#prezzo').textContent = result.prezzo.toFixed(2);
      closedSec.querySelector('#nomeAcquirente').textContent = result.nomeAcquirente;
      closedSec.querySelector('#indirizzo').textContent = result.indirizzo;
    }

    // Mostra la pagina solo dopo il rendering
    page.hidden = false;
  };

  xhr.onerror = function() {
    alert('Errore nella fetch dei dettagli asta.');
  };

  xhr.send();
}

// Richiama la servlet per chiudere un'asta
export function handlerCloseAsta(idAsta) {
  const xhr = new XMLHttpRequest();
  xhr.open('POST', '/TIW-Project/DettaglioAstaChiudiAstaServlet', true);
  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      let msg = 'Errore nella chiusura dell\'asta';
      try {
        const err = JSON.parse(xhr.responseText);
        msg = err.error || msg;
      } catch (_) {}
      alert(msg);
      return;
    }

	// imposta ultima azione
	setCookie("lastAction",  {"value" : "closedAsta"} , 30);
	
    // Ricarica dettagli per mostrare stato aggiornato
    loadDettaglioAsta(idAsta);
  };

  xhr.onerror = function() {
    alert('Errore nella chiusura dell\'asta.');
  };

  // Invia parametro
  xhr.send(`idAsta=${encodeURIComponent(idAsta)}`);
}
