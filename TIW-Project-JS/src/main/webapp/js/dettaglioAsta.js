import { hideAllPages, showVendo} from './main.js';

// Carica e renderizza i dettagli di un'asta
export function renderDettaglioAstaPage(idAsta) {

  // Nasconde tutte le pagine
  hideAllPages();
  
  //gestione del bottone "Indietro"
  const back = document.getElementById('back');
  back.addEventListener('click', e => {
    e.preventDefault();
    showVendo();
    back.removeEventListener('click', showVendo);
  });
  //mostro il bottone
  back.hidden=false;
  
  //inizio gestione della pagina dettaglioAsta (div)
  const page = document.getElementById('dettaglioAstaPage');

  //Pulisco i campi dinamici in entrambe le sezioni (astaAperta e astaChiusa)

  //creo un array con i nomi dei campi da pulire, li scorro con il forech
  //per ogni campo prendo l'elemento con l'id del campo e lo pulisco
  ['creatore','prezzo','rialzo','dataScadenza','oraScadenza']
    .forEach(f => {
      const el = page.querySelector('#astaAperta #'+f);
      if (el) 
        el.textContent = '';
    });

  ['creatore','prezzoIniziale','rialzoMinimo','dataScadenza','oraScadenza','prezzo','nomeAcquirente','indirizzo']
    .forEach(f => {
      const el = page.querySelector('#astaChiusa #'+f);
      if (el) 
        el.textContent = '';
    });

  // Pulisce la tabella offerte - "azzero" il tbody
  const offTable = document.getElementById('listaOfferteDettAsta');
  if (offTable) 
    offTable.innerHTML = '';

  // Richiesta GET alla servlet
  const xhr = new XMLHttpRequest();
  xhr.open('GET', '/TIW-Project-JS/dettaglioAstaPage?idAsta='+encodeURIComponent(idAsta), true);
  //setto già il tipo di risposta in modo da non dover fare il parsing dopo
  xhr.responseType = 'json';

  //inizio gestione ajax
  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert(`Errore ${xhr.status} nel caricamento della pagina`);
      return;
    }

    const result = xhr.response;

    //inizio a popolare i campi
    const openSec = page.querySelector('#astaAperta');
    const closedSec = page.querySelector('#astaChiusa');

    //se esiste il campo "openAsta" ed è true 
    if (result.openAsta) {
      // Mostra sezione asta aperta
      openSec.hidden = false;
      closedSec.hidden = true;

      // Popola i campi dell'asta aperta
      openSec.querySelector('#creatore').textContent = result.openAsta.creatore;
      openSec.querySelector('#prezzo').textContent = result.openAsta.prezzoIniziale;
      openSec.querySelector('#rialzo').textContent = result.openAsta.rialzoMinimo;
      openSec.querySelector('#dataScadenza').textContent = result.openAsta.dataScadenza;
      openSec.querySelector('#oraScadenza').textContent = result.openAsta.oraScadenza;

      // Popola tabella offerte (tbody)
      const tbody = openSec.querySelector('table tbody');
      result.offerte.forEach(o => {
        const row = tbody.insertRow();
        row.insertCell().textContent = o.utente;
        row.insertCell().textContent = o.prezzo;
        row.insertCell().textContent = o.dataOfferta;
        row.insertCell().textContent = o.oraOfferta;
      });

      // Gestione bottone "Chiudi Asta"
      const canDiv = page.querySelector('#canBeClosed');
      const btn = canDiv.querySelector('button');
      if (result.canBeClosed) {
        canDiv.hidden = false;

        //associo la funzione al bottone
        btn.addEventListener('click', e => {
          e.preventDefault();
          handlerCloseAsta(idAsta);

          //dopo che la funzione parte, tolgo l'event listener
          btn.removeEventListener('click', handlerCloseAsta);
        });
      } else {
        //altrimenti lo lascio nascosto
        canDiv.hidden = true;
      }

    } else {
      // Mostra sezione asta chiusa
      openSec.hidden  = true;
      closedSec.hidden = false;

      //popolo i campi dell'asta chiusa
      closedSec.querySelector('#creatore').textContent = result.astaChiusa.creatore;
      closedSec.querySelector('#prezzoIniziale').textContent = result.astaChiusa.prezzoIniziale;
      closedSec.querySelector('#rialzoMinimo').textContent = result.astaChiusa.rialzoMinimo;
      closedSec.querySelector('#dataScadenza').textContent = result.astaChiusa.dataScadenza;
      closedSec.querySelector('#oraScadenza').textContent = result.astaChiusa.oraScadenza;
      closedSec.querySelector('#prezzo').textContent = result.prezzo;
      closedSec.querySelector('#nomeAcquirente').textContent = result.nomeAcquirente;
      closedSec.querySelector('#indirizzo').textContent = result.indirizzo;
    }

    // Mostra la pagina solo dopo il rendering
    page.hidden = false;
  };

  //se c'è un errore:
  xhr.onerror = function() {
    alert('Errore nella comunicazione per recuperare i dettagli asta.');
  };

  //invio la richiesta http
  xhr.send();
}

// Richiama la servlet corretta per chiudere un'asta
export function handlerCloseAsta(idAsta) {
  const xhr = new XMLHttpRequest();
  xhr.open('POST', '/TIW-Project-JS/chiudiAsta', true);

  //gestione ajax
  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      let msg = 'Errore nella chiusura dell\'asta';
      const err = JSON.parse(xhr.response);
      msg = err.error + msg;
      alert(err.error +' | '+ msg);
      return;
    }
	
    // Ricarica dettagli per mostrare stato aggiornato
    renderDettaglioAstaPage(idAsta);	

  };

  //se c'è un errore:
  xhr.onerror = function() {
    alert('Errore nella chiusura dell\'asta.');
  };

  // Invia la richiesta http
  xhr.send();
}
