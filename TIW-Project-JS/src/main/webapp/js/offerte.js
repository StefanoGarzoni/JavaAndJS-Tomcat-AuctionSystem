import { hideAllPages, showAcquisto } from './main.js';

// rendering della pagina offerta
export function renderOffertaPage(idAsta) {
  hideAllPages();
  
  const back = document.getElementById('back');
 
  back.addEventListener('click', e => {
    e.preventDefault();
    showAcquisto();
    back.removeEventListener('click', showAcquisto);
  });
  
  back.hidden=false;
  
  const page = document.getElementById('offertaPage');

  // Pulisco i campi dinamici (tbody)
  const offTable = document.getElementById('listaOffertePage');
  if (offTable) offTable.innerHTML = '';
  const artTable = document.getElementById('articoliAsta');
  if (artTable) artTable.innerHTML = '';

  // Chiamata GET tramite XMLHttpRequest
  const xhr = new XMLHttpRequest();
  xhr.open('GET', `offertePage?idAsta=${encodeURIComponent(idAsta)}`);
  xhr.responseType = 'json';

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert(`Server risponde ${xhr.status}`);
      return;
    }
    const { articoli, offerte, rialzo_minimo, prezzo_attuale } = xhr.response;

    // Popolo la tabella degli articoli
    articoli.forEach(a => {
      const row = artTable.insertRow();
      row.insertCell().textContent = a.cod;
      row.insertCell().textContent = a.venditore;
      row.insertCell().textContent = a.nomeArticolo;
      row.insertCell().textContent = a.descrizione;
      row.insertCell().textContent = a.prezzo;
    });

    // Popolo la tabella delle offerte
    offerte.forEach(o => {
      const row = offTable.insertRow();
      row.insertCell().textContent = o.utente;
      row.insertCell().textContent = o.prezzo;
      row.insertCell().textContent = o.dataOfferta;
      row.insertCell().textContent = o.oraOfferta;
    });

    // Mostro il rialzo minimo
    document.getElementById('showRialzoMinimo').textContent = rialzo_minimo;

    // Aggancio il bottone per l'inserimento dell'offerta (riclonandolo per rimuovere listener)
    const btn = document.getElementById('submitNewOfferta');
    const newBtn = btn.cloneNode(true);
    btn.replaceWith(newBtn);
    newBtn.addEventListener('click', event =>{
		event.preventDefault();
      handlerAddOfferta(prezzo_attuale, rialzo_minimo);
    });
  };

  xhr.onerror = function() {
    alert('Impossibile caricare i dati dell\'asta.');
  };

  page.hidden = false;

  xhr.send();
}

export function handlerAddOfferta(prezzoAttuale, rialzoMinimo) {
  
  // Seleziono e valido l'input prezzo
  const inputPrezzo = document.getElementById('prezzoOffertaAsta');
  const prezzoUser = parseFloat(inputPrezzo.value);
  const minOfferta = prezzoAttuale + rialzoMinimo;

  if (isNaN(prezzoUser) || prezzoUser < minOfferta) {
    alert(`Offerta minima: ${minOfferta}€`);
    return;
  }

  // Costruisco body URL-encoded
  const formData = new FormData();
  formData.append('prezzo', prezzoUser);

  // Chiamata POST tramite XMLHttpRequest
  const xhr = new XMLHttpRequest();
  xhr.open('POST', '/TIW-Project-JS/offertaAdd');
  //imposto gli header??

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert(`Errore: ${xhr.status}`);
      return;
    }

    try {
      // Parsing dell'oggetto Offerta restituito dalla servlet
      const newOfferta = JSON.parse(xhr.response);
      const table = document.getElementById('listaOffertePage');

      // Approccio meno verbose: insertRow + insertCell
      const row = table.insertRow();
      row.insertCell().textContent = newOfferta.utente;
      row.insertCell().textContent = newOfferta.prezzo;
      row.insertCell().textContent = newOfferta.dataOfferta;
      row.insertCell().textContent = newOfferta.oraOfferta;
      //row.insertCell().textContent = newOfferta.idOfferta;

      // Ripulisco input
      inputPrezzo.value = '';
      //inputPrezzo.focus();
	  
	  //setCookie("lastAction", "addedOfferta", 30);

    } catch (e) {
      console.error('Errore parsing risposta JSON', e);
      alert('Risposta non valida dal server.');
    }
  };

  xhr.onerror = function() {
    alert("Errore durante l'invio dell'offerta.");
  };

  xhr.send(formData);
}

