/**
 * Nasconde tutte le "pagine" (div) dell'applicazione single-page.
 */
export function hideAllPages() {
  document.getElementById('vendoPage').hidden = true;
  document.getElementById('acquistoPage').hidden = true;
  document.getElementById('dettaglioAstaPage').hidden = true;
  document.getElementById('offertaPage').hidden = true;
}

/**
 * Rende visibile e popola la pagina di offerta per l'asta specificata.
 * @param {number|string} idAsta - Identificativo dell'asta da visualizzare
 */
export async function renderOffertaPage(idAsta) {
  // 1. Nascondi tutte le altre pagine
  hideAllPages();

  // 2. Mostra il contenitore offertaPage
  const offertaPage = document.getElementById('offertaPage');
  offertaPage.hidden = false;
  offertaPage.innerHTML = ''; // Pulisci contenuto precedente

  try {
    // 3. Richiedi i dati dell'asta in formato JSON dal server
    const res = await fetch(`OfferteJsonServlet?idAsta=${idAsta}`);
    if (!res.ok) throw new Error(`Errore ${res.status} nel caricamento dati offerta`);
    const data = await res.json();
    const { articoli, offerte, rialzo_minimo, prezzo_attuale } = data;

    // 4. Titolo e informazioni base dell'asta
    const title = document.createElement('h2');
    title.textContent = `Dettaglio Asta #${idAsta}`;
    offertaPage.appendChild(title);

    // 5. Tabella articoli
    const artTable = document.createElement('table');
    artTable.innerHTML = `
      <thead>
        <tr>
          <th>Codice</th>
          <th>Nome</th>
          <th>Prezzo</th>
        </tr>
      </thead>
      <tbody>
        ${articoli.map(a => `
          <tr>
            <td>${a.codice}</td>
            <td>${a.nome}</td>
            <td>${a.prezzo.toFixed(2)}€</td>
          </tr>
        `).join('')}
      </tbody>
    `;
    offertaPage.appendChild(artTable);

    // 6. Elenco offerte (ordinate per data+ora decrescente)
    const offTable = document.createElement('table');
    offTable.innerHTML = `
      <h3>Offerte ricevute</h3>
      <thead>
        <tr>
          <th>Utente</th>
          <th>Prezzo</th>
          <th>Data e ora</th>
        </tr>
      </thead>
      <tbody>
        ${offerte.map(o => `
          <tr>
            <td>${o.username}</td>
            <td>${o.prezzo.toFixed(2)}€</td>
            <td>${new Date(o.timestamp).toLocaleString()}</td>
          </tr>
        `).join('')}
      </tbody>
    `;
    offertaPage.appendChild(offTable);

    // 7. Form per inserire una nuova offerta
    const minBid = prezzo_attuale + rialzo_minimo;
    const formDiv = document.createElement('div');
    formDiv.innerHTML = `
      <h3>Inserisci la tua offerta</h3>
      <label>Prezzo (minimo ${minBid.toFixed(2)}€):</label>
      <input type="number" id="prezzoOfferta" step="0.01" min="${minBid.toFixed(2)}" required>
      <button id="submitOfferta">Invia Offerta</button>
      <span id="offertaError" style="color:red;margin-left:1em;"></span>
    `;
    offertaPage.appendChild(formDiv);

    // 8. Gestione click su "Invia Offerta"
    document.getElementById('submitOfferta').addEventListener('click', async () => {
      const input = document.getElementById('prezzoOfferta');
      const prezzoUser = parseFloat(input.value);
      const errorSpan = document.getElementById('offertaError');
      errorSpan.textContent = '';

      if (isNaN(prezzoUser) || prezzoUser < minBid) {
        errorSpan.textContent = `Devi inserire almeno ${minBid.toFixed(2)}€`;
        return;
      }

      try {
        // Invia POST al server per inserire l'offerta
        const postRes = await fetch('AddOffertaJsonServlet', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ idAsta, prezzo: prezzoUser })
        });
        if (!postRes.ok) throw new Error(`Errore ${postRes.status} nell'invio offerta`);

        // Dopo inserimento, ricarica la pagina offerta
        renderOffertaPage(idAsta);
      } catch (err) {
        errorSpan.textContent = 'Errore durante l'invio dell'offerta.';
        console.error(err);
      }
    });

  } catch (err) {
    offertaPage.textContent = 'Impossibile caricare i dati dell'asta.';
    console.error(err);
  }
}
