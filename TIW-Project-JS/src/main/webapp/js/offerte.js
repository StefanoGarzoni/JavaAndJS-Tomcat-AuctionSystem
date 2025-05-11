// FUNZIONI PER LA PAGINA OFFERTE

// funzione comune a tutte le pagine per nascondere tutto
export function hideAllPages() {
  document.getElementById('vendoPage').hidden = true;
  document.getElementById('acquistoPage').hidden = true;
  document.getElementById('dettaglioAstaPage').hidden = true;
  document.getElementById('offertaPage').hidden = true;
}

// rendering della pagina offerta usando XMTHttpRequest
export function renderOffertaPage(idAsta) {
  hideAllPages();
  const page = document.getElementById('offertaPage');

  // Pulisco i campi dinamici
  const listaOff = document.getElementById('listaOfferte');
  if (listaOff) listaOff.innerHTML = '';
  const listaArt = document.getElementById('articoliAsta');
  if (listaArt) listaArt.innerHTML = '';

  // Seleziono le due tabelle (prima = articoli, seconda = offerte)
  const [artTable, offTable] = page.getElementsByTagName('table');
  page.hidden = false;

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
      handlerAddOfferta(event, idAsta, prezzo_attuale, rialzo_minimo)
    );
  };

  xhr.onerror = function() {
    alert('Impossibile caricare i dati dell\'asta.');
  };

  xhr.send();
}

// gestione dell'aggiunta di una nuova offerta usando XMTHttpRequest
export function handlerAddOfferta(event, idAsta, prezzoAttuale, rialzoMinimo) {
  event.preventDefault();

  // Seleziono e valido l'input prezzo
  const inputPrezzo = document.getElementById('prezzo');
  const prezzoUser = parseFloat(inputPrezzo.value);
  const minOfferta = prezzoAttuale + rialzoMinimo;

  if (isNaN(prezzoUser) || prezzoUser < minOfferta) {
    alert('Offerta minima: ' + minOfferta.toFixed(2) + '€');
    return;
  }

  // Costruisco body URL-encoded
  const params = new URLSearchParams();
  params.set('prezzo', prezzoUser);

  // Chiamata POST tramite XMTHttpRequest
  const xhr = new XMTHttpRequest();
  xhr.open('POST', 'AddOffertaServlet');
  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert(xhr.status);
      return;
    }
    // Al successo, ricarico la pagina offerta
    renderOffertaPage(idAsta);
  };

  xhr.onerror = function() {
    alert('Errore durante l\'invio dell\'offerta.');
  };

  xhr.send(params.toString());
}
