//funzione comune a tutte le pagine per nascondere tutto
export function hideAllPages() {
  document.getElementById('vendoPage').hidden = true;
  document.getElementById('acquistoPage').hidden = true;
  document.getElementById('dettaglioAstaPage').hidden = true;
  document.getElementById('offertaPage').hidden = true;
}

export function loadDettaglioAsta(idAsta) {
  hideAllPages();
  const page = document.getElementById('dettaglioAstaPage');
  
  //pulisco solo i campi dinamici
  ['creatore','prezzo','rialzo','dataScadenza','oraScadenza'].forEach(id => {
    const el = page.querySelector(`#astaAperta #${id}`);
    if (el) el.textContent = '';
  });

  ['creatore','prezzoIniziale','rialzoMinimo','dataScadenza','oraScadenza',
   'prezzo','nomeAcquirente','indirizzo'
  ].forEach(id => {
    const el = page.querySelector(`#astaChiusa #${id}`);
    if (el) el.textContent = '';
  });

  const lista = document.getElementById('listaOfferte');
  if (lista) lista.innerHTML = '';

  
  //mostro la pagina
  page.hidden = false;

  const xhr = new XMLHttpRequest();
  xhr.open('GET', `/TIW-Project/DettaglioAstaPageServlet?idAsta=${encodeURIComponent(idAsta)}`, true);
  xhr.responseType = 'json';

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert(xhr.status + ' Errore nel caricamento della pagina');
      return;
    }
    const result = xhr.response;

    if (result.openAsta) {
      // Asta aperta
      document.getElementById('astaAperta').hidden = false;
      document.getElementById('astaChiusa').hidden = true;

      // Popola i campi dell'asta aperta
      document.querySelector('#astaAperta #creatore').textContent = result.openAsta.creatore;
      document.querySelector('#astaAperta #prezzo').textContent = result.openAsta.prezzoIniziale;
      document.querySelector('#astaAperta #rialzo').textContent = result.openAsta.rialzoMinimo;
      document.querySelector('#astaAperta #dataScadenza').textContent = result.openAsta.dataScadenza;
      document.querySelector('#astaAperta #oraScadenza').textContent = result.openAsta.oraScadenza;

      // Tabella offerte
      const tbody = document.querySelector('#astaAperta table tbody');
      tbody.innerHTML = '';
      for (const offerta of result.offerte) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td>${offerta.username}</td>
          <td>${offerta.prezzo}</td>
          <td>${offerta.data}</td>
          <td>${offerta.ora}</td>
        `;
        tbody.appendChild(tr);
      }

      // Gestione bottone "Chiudi Asta"
      const canBeClosedDiv = document.getElementById('canBeClosed');
      const oldButton = canBeClosedDiv.querySelector('button');
      if (result.canBeClosed) {
        canBeClosedDiv.hidden = false;

        // Clone del bottone 
        const newButton = oldButton.cloneNode(true);
        // Sostituisce il vecchio con il clone per eliminare vecchi event collegati (prevenzione)
        oldButton.replaceWith(newButton);

        // Aggancia il bottone all'evento
        newButton.addEventListener('click', function(e) {
          e.preventDefault();
          closeAsta(idAsta);
        });

      } else {
        canBeClosedDiv.hidden = true;
      }

    } else {
      // Asta chiusa
      document.getElementById('astaAperta').hidden = true;
      document.getElementById('astaChiusa').hidden = false;

      // Popola i campi dell'asta chiusa
      document.querySelector('#astaChiusa #creatore').textContent = result.astaChiusa.creatore;
      document.querySelector('#astaChiusa #prezzoIniziale').textContent = result.astaChiusa.prezzoIniziale;
      document.querySelector('#astaChiusa #rialzoMinimo').textContent = result.astaChiusa.rialzoMinimo;
      document.querySelector('#astaChiusa #dataScadenza').textContent = result.astaChiusa.dataScadenza;
      document.querySelector('#astaChiusa #oraScadenza').textContent = result.astaChiusa.oraScadenza;
      document.querySelector('#astaChiusa #prezzo').textContent = result.prezzo;
      document.querySelector('#astaChiusa #nomeAcquirente').textContent = result.nomeAcquirente;
      document.querySelector('#astaChiusa #indirizzo').textContent = result.indirizzo;
    }
  };

  xhr.onerror = function() {
    alert('Errore nella fetch dei dettagli asta.');
  };

  xhr.send();
}

export function closeAsta(idAsta) {
  // Chiama DettaglioAstaChiudiAstaServlet
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

    // Ricarica i dettagli per mostrare lo stato "chiusa"
    loadDettaglioAsta(idAsta);
  };

  xhr.onerror = function() {
    alert('Errore nella chiusura dell\'asta.');
  };

  xhr.send();
}
