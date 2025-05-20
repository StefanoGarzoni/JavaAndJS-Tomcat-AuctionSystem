import { renderAcquistoPage } from './acquisto.js';
import { hideAllPages, showAcquisto } from './main.js';

// rendering della pagina offerta
export function renderOffertaPage(idAsta) {

  //nasconfo tutti i div da home.html
  hideAllPages();
  
  // gestisco il bottone per tornare alla pagina di acquisto
  const back = document.getElementById('back');
 
  back.addEventListener('click', e => {
    e.preventDefault();
    showAcquisto();
    back.removeEventListener('click', showAcquisto);
  });
  
  //mostro il bottone appena gestito
  back.hidden=false;
  
  //inizio a lavorare sul contenuto del div gestito da questa pagina js
  const page = document.getElementById('offertaPage');

  // Pulisco i campi dinamici delle tabelle (tbody)
  const offTable = document.getElementById('listaOffertePage');
  if (offTable) 
    offTable.innerHTML = '';

  const artTable = document.getElementById('articoliAsta');
  if (artTable) 
    artTable.innerHTML = '';

  // Chiamata GET tramite XMLHttpRequest: chiamo la servelt per la pagina offerta passandogli l'id dell'asta
  const xhr = new XMLHttpRequest();
  xhr.open('GET', `offertePage?idAsta=${encodeURIComponent(idAsta)}`);

  //dicendogli che voglio una risposta in formato JSON, non dovrò fare io manualmente il parsing della response
  xhr.responseType = 'json';


  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert('Server risponde: '+xhr.status);
      return;
    }

    //xhr.response; è convertito in un oggetto JS da JSON grazie al fatto che gli ho detto il tipo della risposta
    //poi uso il destructuring assignment per estrarre i dati (nota come i nomi devono combaciare)
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


    /*
    1. btn.cloneNode(true);
      cloneNode è un metodo DOM che crea una copia di un nodo (in questo caso, un elemento <button>).
      L’argomento true indica che la copia sarà profonda (deep clone): verranno copiati anche tutti 
      i nodi figli e il loro contenuto (testo, eventuali elementi HTML interni).
      Importante: gli event listener associati al nodo originale non vengono copiati. 
      Questo è spesso usato proprio per "pulire" i listener precedenti.
      Esempio pratico:
      Se il bottone aveva listener aggiunti in precedenza, il clone non li avrà. 
      Così puoi aggiungere solo quelli che ti servono ora, evitando duplicazioni o comportamenti indesiderati.

    2. btn.replaceWith(newBtn);
      replaceWith è un metodo DOM che sostituisce il nodo corrente (btn) con un altro nodo (newBtn) nel DOM.
      Dopo questa chiamata, il bottone originale viene rimosso dalla pagina e al suo posto c’è il nuovo bottone clonato.
    */
    const newBtn = btn.cloneNode(true);
    btn.replaceWith(newBtn);

    newBtn.addEventListener('click', event =>{
		  event.preventDefault();
      handlerAddOfferta(prezzo_attuale, rialzo_minimo);
    });
  };

  //ogni volta che durante la richiesta AJAX si verifica un errore, viene chiamata questa funzione ( un alert )
  xhr.onerror = function() {
    alert('Impossibile caricare i dati dell\'asta.');
  };

  //mostro la pagina (il div dedicato)
  page.hidden = false;

  //invio la richiesta http
  xhr.send();
}

// Funzione per gestire l'inserimento dell'offerta
export function handlerAddOfferta(prezzoAttuale, rialzoMinimo) {
  
  // Seleziono e valido l'input prezzo
  const inputPrezzo = document.getElementById('prezzoOffertaAsta');
  const prezzoUser = parseFloat(inputPrezzo.value);
  const minOfferta = prezzoAttuale + rialzoMinimo;

  if (isNaN(prezzoUser) || prezzoUser < minOfferta) {
    alert('Offerta minima di '+minOfferta+'€ - non rispettata');
    return;
  }

  // Costruisco body URL-encoded - voglio inviare i dati in post come un form
  /*
  FormData è un oggetto nativo di JavaScript che permette di costruire facilmente 
  una serie di coppie chiave/valore da inviare tramite una richiesta HTTP, tipicamente con metodo POST.
  Viene usato soprattutto per inviare dati di form (anche file) in modo semplice e sicuro, 
  senza doverli serializzare manualmente.
  */
  const formData = new FormData();
  formData.append('prezzo', prezzoUser);

  // Chiamata POST tramite XMLHttpRequest
  const xhr = new XMLHttpRequest();
  xhr.open('POST', '/TIW-Project-JS/offertaAdd');

  xhr.onload = function() {
    if (xhr.status < 200 || xhr.status >= 300) {
      alert('Server risponde: '+xhr.status);
      return;
    }

    const newOfferta = JSON.parse(xhr.response);
    if (newOfferta.errorChiusura) {
      alert('Errore: ' + newOfferta.errorChiusura);
      renderAcquistoPage();
      return;
    }

    try {
      // Parsing dell'oggetto Offerta restituito dalla servlet
      //NOTA: avessi inserito xhr.responseType = 'json'; non avrei dovuto fare il parsing manualmente
      //teniamo comunque questao processo per mostrare le varie possibilità di tecniche usabili

      const table = document.getElementById('listaOffertePage');

      //inserisco il campo nella tabella (la nuova offerta nella tabella delle offerte)
      const row = table.insertRow();
      row.insertCell().textContent = newOfferta.utente;
      row.insertCell().textContent = newOfferta.prezzo;
      row.insertCell().textContent = newOfferta.dataOfferta;
      row.insertCell().textContent = newOfferta.oraOfferta;
      //row.insertCell().textContent = newOfferta.idOfferta;

      // Ripulisco input
      inputPrezzo.value = '';

    } catch (e) {
      console.error('Errore parsing risposta JSON', e);
      alert('Risposta non valida dal server | Errore parsing risposta JSON : '+e);
    }
  };

  xhr.onerror = function() {
    alert("Errore durante l'invio dell'offerta.");
  };


  /*
  Come si usa formdata con XMLHttpRequest:
    Quando passi un oggetto FormData come corpo di una richiesta POST, 
    il browser imposta automaticamente l’header Content-Type a multipart/form-data (con boundary corretto) 
    e serializza i dati come farebbe un form HTML.

    per questo motivo la servlet deve avere i campi:
    @MultipartConfig(
	    fileSizeThreshold = 1024 * 1024 * 100,      // 100MB in RAM
	    maxFileSize = 1024 * 1024 * 100,       // 100MB per file
	    maxRequestSize = 1024 * 1024 * 500     // 500MB in totale
	  )
  */
  xhr.send(formData);
}

