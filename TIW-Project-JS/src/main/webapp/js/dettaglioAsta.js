//funzione comune a tutte le pagine per nascondere tutto
export function hideAllPages() {
  document.getElementById('vendoPage').hidden = true;
  document.getElementById('acquistoPage').hidden = true;
  document.getElementById('dettaglioAstaPage').hidden = true;
  document.getElementById('offertaPage').hidden = true;
}

export async function loadDettaglioAsta(idAsta) {
    hideAllPages();
    const page = document.getElementById('dettaglioAstaPage');
    page.hidden = false;
    page.innerHTML = ''; 
  
    try {
    const response = await fetch(
      `/TIW-Project/DettaglioAstaPageServlet?idAsta=${encodeURIComponent(idAsta)}`
    );
    if (!response.ok) {
        alert(response.status+' Errore nel caricamento della pagina');
        return;
    }
    const result = await response.json();

    if (result.openAsta) {
      // Asta aperta
      document.getElementById('astaAperta').hidden = false;
      document.getElementById('astaChiusa').hidden = true;

      // Popola i campi dell'asta aperta
      document.querySelector('#astaAperta #creatore').textContent = result.openAsta.creatore;
      document.querySelector('#astaAperta #prezzo').textContent = result.openAsta.prezzoIniziale;
      document.querySelector('#astaAperta #rialzo').textContent  = result.openAsta.rialzoMinimo;
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
        newButton.addEventListener('click', async (e) => {
          e.preventDefault();
          await closeAsta();
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

  } catch (err) {
    alert(err+' Errore nella fetch dei dettagli asta.');
  }
}

export async function closeAsta() {
  try {
    // Chiama DettaglioAstaChiudiAstaServlet
    const response = await fetch(
      '/TIW-Project/DettaglioAstaChiudiAstaServlet', 
      { method: 'POST' }
    );
    if (!response.ok) {
      const error = await response.json();
      alert(error.error || 'Errore nella chiusura dell\'asta');
      return;
    }

    // Ricarica i dettagli per mostrare lo stato "chiusa"
    await loadDettaglioAsta();

  } catch (err) {
    alert('Errore nella fetch per chiudere l\'asta: ' + err);
  }
}
