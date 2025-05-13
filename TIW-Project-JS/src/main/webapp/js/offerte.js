import { hideAllPages } from './main.js';

// rendering della pagina offerta
export function renderOffertaPage(idAsta) {
  hideAllPages();
  const page = document.getElementById('offertaPage');

  // Pulisco i campi dinamici (tbody)
  const offTable = document.getElementById('listaOfferte');
  if (offTable) offTable.innerHTML = '';
  const artTable = document.getElementById('articoliAsta');
  if (artTable) artTable.innerHTML = '';

  // Chiamata GET tramite XMTHttpRequest
  const xhr = new XMTHttpRequest();
  xhr.open('GET', `OfferteServlet?idAsta=${encodeURIComponent(idAsta)}`);
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
      row.insertCell().textContent = a.codice;
      row.insertCell().textContent = a.venditore;
      row.insertCell().textContent = a.nomeArticolo;
      row.insertCell().textContent = a.descrizione;
      row.insertCell().textContent = a.prezzo.toFixed(2);
    });

    // Popolo la tabella delle offerte
    offerte.forEach(o => {
      const row = offTable.insertRow();
      row.insertCell().textContent = o.username;
      row.insertCell().textContent = o.prezzo.toFixed(2);
      row.insertCell().textContent = o.dataOfferta;
      row.insertCell().textContent = o.oraOfferta;
    });

    // Mostro il rialzo minimo
    document.getElementById('rialzoMinimo').textContent = rialzo_minimo.toFixed(2);

    // Aggancio il bottone per l'inserimento dell'offerta (riclonandolo per rimuovere listener)
    const btn = document.getElementById('submitNewOfferta');
    const newBtn = btn.cloneNode(true);
    btn.replaceWith(newBtn);
    newBtn.addEventListener('click', event =>
      handlerAddOfferta(event, prezzo_attuale, rialzo_minimo)
    );
  };

  xhr.onerror = function() {
    alert('Impossibile caricare i dati dell\'asta.');
  };

  page.hidden = false;

  xhr.send();
}

export function handlerAddOfferta(event, prezzoAttuale, rialzoMinimo) {
  event.preventDefault();

  // Seleziono e valido l'input prezzo
  const inputPrezzo = document.getElementById('prezzo');
  const prezzoUser = parseFloat(inputPrezzo.value);
  const minOfferta = prezzoAttuale + rialzoMinimo;

  if (isNaN(prezzoUser) || prezzoUser < minOfferta) {
    alert(`Offerta minima: ${minOfferta.toFixed(2)}€`);
    return;
  }

  // Costruisco body URL-encoded
  const formData = new FormData();
  formData.append('prezzo', inputPrezzo.value);

  // Chiamata POST tramite XMLHttpRequest
  const xhr = new XMLHttpRequest();
  xhr.open('POST', 'AddOffertaServlet');
  //imposto gli header??

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert(`Errore: ${xhr.status}`);
      return;
    }

    try {
      // Parsing dell'oggetto Offerta restituito dalla servlet
      const newOfferta = JSON.parse(xhr.response);
      const table = document.getElementById('listaOfferte');

      // Approccio meno verbose: insertRow + insertCell
      const row = table.insertRow();
      row.insertCell().textContent = newOfferta.username;
      row.insertCell().textContent = parseFloat(newOfferta.prezzo).toFixed(2) + '€';
      row.insertCell().textContent = newOfferta.data;
      row.insertCell().textContent = newOfferta.ora;
      row.insertCell().textContent = newOfferta.id;

      // Ripulisco input
      inputPrezzo.value = '';
      //inputPrezzo.focus();
	  
	  setCookie("lastAction", "addedOfferta", 30);

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

