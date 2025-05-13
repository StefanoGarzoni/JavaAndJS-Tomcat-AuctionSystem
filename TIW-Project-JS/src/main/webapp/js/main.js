import { renderVendoPage, setupPageVendo} from './vendo.js';
import { renderAcquistoPage, setupPageAscquisto } from './acquisto.js';

<<<<<<< HEAD
//"Menu"
=======
// aggiungo gli event listeners
>>>>>>> 0579f4cdeb4135d3d81edf6790382af89e1b31f8
document.addEventListener('DOMContentLoaded', () => {
    const moveToVendo = document.getElementById('moveToVendo');
    const moveToAcquisto = document.getElementById('moveToAcquisto');

    moveToVendo.addEventListener('click', () => {
        renderVendoPage();
    });
    moveToAcquisto.addEventListener('click', () => {
        renderAcquistoPage();
    });
	
	// inizializzazione cookie flag che indicano se è necessario ricaricare le aste nella pagina vendo
	cookieSetup();

<<<<<<< HEAD

    if (getCookie('lastAction') === 'addedAsta') {
=======
    // reindirizzamento in base all'ultima azione svolta dall'utente
    const lastAction = getCookie("lastAction").value;
    if (lastAction === 'addedAsta') {
>>>>>>> 0579f4cdeb4135d3d81edf6790382af89e1b31f8
        showVendo();
    } else {
        showAcquisto();
    }

});

function cookieSetup(){
	let cookieNames = ['renderTableAsteAperte', 'renderTableAsteChiuse', 'renderArticoli', 'renderTableAsteVisionate'];
	
	for (let cookieName of cookieNames) {
		// quando viene caricato il main (l'applicazione viene aperta) bisogna inizializzare i flag a true 
		// poichè la pagina vendo deve per forza richiedere tutte le aste al server
		setCookie(cookieName, {"value" : true}, 30);
	}
}

export function setCookie(name, value, days) {
    const expiration = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString(); 		// calcolo scadenza a days giorni di distanza
    document.cookie = `${name}=${encodeURIComponent(JSON.stringify(value))};expires=${expiration};path=/`;
}

<<<<<<< HEAD
// Dichiaro i tre nomi dei cookie
var cookieNames = ['renderTableAsteAperte', 'renderAllTablesAste'];

//scadenza di una settimana
var oneWeek = 7 * 24 * 60 * 60;

//scorro tutti i cookie
for (var i = 0; i < cookieNames.length; i++) {
    document.cookie = 
        cookieNames[i] + '=' + encodeURIComponent("True") +
        '; max-age=' + oneWeek +
        '; path=/';
=======
export function getCookie(name) {
    const cookies = document.cookie.split(';');	// divide tutti i cookies
    
	for (const cookie of cookies) {
        const [key, value] = cookie.trim().split('=');	// divide il nome e il valore del cookies
        if (key === name) {
			return JSON.parse(decodeURIComponent(value));	// restituisce il valore corrente del cookie richiesto, come oggetto JSON
        }
    }
    return null;
>>>>>>> 0579f4cdeb4135d3d81edf6790382af89e1b31f8
}

// Show "Vendo" pages
function showVendo() {
    moveToAcquisto.removeAttribute('hidden');
    moveToVendo.setAttribute('hidden', true);
    hideAllPages();
    renderVendoPage();
}

// Show "Acquisto" page
function showAcquisto() {
    moveToVendo.removeAttribute('hidden');
    moveToAcquisto.setAttribute('hidden', true);
    hideAllPages();
    renderAcquistoPage();
}

<<<<<<< HEAD

//funzione per salvare la lista di idAsta visitate
export function saveVisited(idAsta) {
    const key = 'asteLastVisited';
    var oneWeek = 7 * 24 * 60 * 60;
    // Leggi il cookie e parsa l'array, oppure crea uno vuoto
    const stored = getCookie(key);
    const visits = stored ? JSON.parse(stored) : [];

    // Se non c'è già, aggiungi l'id e riscrivi il cookie
    if (!visits.includes(idAsta)) {
        visits.push(idAsta);
        document.cookie = 
            key + '=' + encodeURIComponent(visits) +
            '; max-age=' + oneWeek +
            '; path=/';
    }
}


=======
>>>>>>> 0579f4cdeb4135d3d81edf6790382af89e1b31f8
export function hideAllPages() {
    document.getElementById('vendoPage').hidden = true;
    document.getElementById('acquistoPage').hidden = true;
    document.getElementById('dettaglioAstaPage').hidden = true;
    document.getElementById('offertaPage').hidden = true;
}

export function getCookie(name) {
  // Prendi tutti i cookie come stringa e spezzali in array di "nome=valore"
    const cookieArray = document.cookie.split('; ');
    
    // Scorri l’array fino a trovare quello giusto
    for (let i = 0; i < cookieArray.length; i++) {
        const cookiePair = cookieArray[i].split('=');
        const key = cookiePair[0];
        // Riassembla eventuali '=' nel valore
        const value = cookiePair.slice(1).join('=');
        
        if (key === name) {
            
            return decodeURIComponent(value);
        }
    }
  
  // Se non trovato, restituisci null
  return null;
}