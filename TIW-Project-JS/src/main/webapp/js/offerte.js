//FUNZIONI PER LA PAGINA OFFERTE

//funzione comune a tutte le pagine per nascondere tutto
export function hideAllPages() {
  document.getElementById('vendoPage').hidden = true;
  document.getElementById('acquistoPage').hidden = true;
  document.getElementById('dettaglioAstaPage').hidden = true;
  document.getElementById('offertaPage').hidden = true;
}

function clearTable(table) {
  // elimina tutte le righe dopo la prima (<tr> header)
  while (table.rows.length > 1) {
    table.deleteRow(1);
  }
}

export async function renderOffertaPage(idAsta) {
  //Nasconde tutte le "pagine" e mostra solo offertaPage
  hideAllPages();
  const page = document.getElementById('offertaPage');
  page.hidden = false;
  page.innerHTML = ''; //resetta il contenuto della pagina

  //Seleziono le due tabelle (prima = articoli, seconda = offerte)
  const [artTable, offTable] = page.getElementsByTagName('table');
  clearTable(artTable);
  clearTable(offTable);

  try {
    //Fetch JSON da OfferteServlet (che ora restituisce { articoli, offerte, rialzo_minimo, prezzo_attuale })
    const res = await fetch(`OfferteServlet?idAsta=${encodeURIComponent(idAsta)}`);
    if (!res.ok) throw new Error(`Server risponde ${res.status}`);
    const { articoli, offerte, rialzo_minimo, prezzo_attuale } = await res.json();

    //Popolo la tabella degli articoli
    //    colonne: Codice, Venditore, Nome Articolo, Descrizione, Prezzo
    articoli.forEach(a => {
      const row = artTable.insertRow();
      row.insertCell().textContent = a.codice;
      row.insertCell().textContent = a.venditore;         
      row.insertCell().textContent = a.nomeArticolo;
      row.insertCell().textContent = a.descrizione;
      row.insertCell().textContent = a.prezzo.toFixed(2);
    });

    //Popolo la tabella delle offerte
    //    colonne: Utente, Prezzo, Data, Ora
    offerte.forEach(o => {
      const row = offTable.insertRow();
      row.insertCell().textContent = o.username;
      row.insertCell().textContent = o.prezzo.toFixed(2);
      row.insertCell().textContent = o.dataOfferta;
      row.insertCell().textContent = dt.oraOfferta;
    });

    //Mostro il rialzo minimo
    document.getElementById('rialzoMinimo').textContent = rialzo_minimo.toFixed(2);
    
    //addevent listener al bottone per l'inserimento dell'offerta
    const btn = document.getElementById('submitNewOfferta');
    btn.addEventListener('click', event =>
      handlerAddOfferta(event, idAsta, prezzoAttuale, rialzoMinimo)
  );


  } catch (e) {
    alert(e.error + 'Impossibile caricare i dati dell\'asta.');
  }
}

export async function handlerAddOfferta(event, idAsta, prezzoAttuale, rialzoMinimo) {
  event.preventDefault();

  // Seleziono gli elementi
  const inputPrezzo = document.getElementById('prezzo');
  
  // Parsing e validazione
  const prezzoUser = parseFloat(inputPrezzo.value);
  const minOfferta = prezzoAttuale + rialzoMinimo;

  if (isNaN(prezzoUser) || prezzoUser < minOfferta) {
    alert('Offerta minima:'+ minOfferta.toFixed(2)+'€');
    return;
  }

  try {
    // Costruisco body
    const params = new URLSearchParams();
    params.set('prezzo', prezzoUser);

    // Chiamo la servlet che inserisce l'offerta in sessione legge idAsta da sessione 
    const postRes = await fetch('AddOffertaServlet', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body:   params.toString()
    });

    if (!postRes.ok) {
      alert(postRes.status);
      throw new Error(`Errore ${postRes.status}`);
    }

    // Al successo, ricarico la pagina offerta
    await renderOffertaPage(idAsta);

  } catch (err) {
    console.error(err);
    alert(err.error + ' Errore durante l\'invio dell\'offerta.');
  }
}