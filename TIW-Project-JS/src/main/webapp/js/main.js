import { setupPageVendo} from './vendo.js';
import { setupPageAscquisto } from './acquisto.js';

// Navigation elements
document.addEventListener('DOMContentLoaded', () => {
    const moveToVendo = document.getElementById('moveToVendo');
    const moveToAcquisto = document.getElementById('moveToAcquisto');

    moveToVendo.addEventListener('click', () => {
        showVendo();
    });
    moveToAcquisto.addEventListener('click', () => {
        showAcquisto();
    });

    // Initial view based on last action
    const last = localStorage.getItem('lastAction');
    if (last === 'addedAsta') {
        showVendo();
    } else {
        showAcquisto();
    }

});


//--------------------------------------------------------------------------------------------
//CREAZIONE INIZIALE DEI COOKIE

// Dichiaro i tre nomi dei cookie
var cookieNames = ['lastAction', 'renderTableAsteAperte', 'renderAllTablesAste'];

//scadenza di una settimana
var oneWeek = 7 * 24 * 60 * 60;

// Leggo tutti i cookie dal browser in un array
var allCookies = document.cookie ? document.cookie.split('; ') : [];

//scorro tutti i cookie
for (var i = 0; i < cookieNames.length; i++) {
    var name = cookieNames[i];
    var exists = false;
    var currentValue = '';

    //Cerco tra tutti i cookie se c'è un name che coincide
    for (var j = 0; j < allCookies.length; j++) {
        var pair = allCookies[j].split('=');
        var key = pair[0];
        var val = pair[1] || '';
        
        if (key === name) {
            exists = true;
            // decodifico il valore per sicurezza
            currentValue = decodeURIComponent(val);
            break;
        }
    }

    // 4b) Se esiste, rinnovo soltanto la scadenza
    if (exists) {
        // ricreo la stringa di set-cookie mantenendo lo stesso valore
        document.cookie = 
        name + '=' + encodeURIComponent(currentValue) +
        '; path=/; max-age=' + oneWeek;

    // 4c) Se non esiste, lo creo con valore "true"
    } else {
        document.cookie = 
        name + '=true' +
        '; path=/; max-age=' + oneWeek;
    }
}
//--------------------------------------------------------------------------------------------


// Show "Vendo" page
function showVendo() {
    moveToAcquisto.removeAttribute('hidden');
    moveToVendo.setAttribute('hidden', true);
    hideAllPages();
    setupPageVendo();
}

// Show "Acquisto" page
function showAcquisto() {
    moveToVendo.removeAttribute('hidden');
    moveToAcquisto.setAttribute('hidden', true);
    hideAllPages();
    setupPageAscquisto();
}


//funzione per salvare la lista di idAsta visitate
export function saveVisited(idAsta) {
    const key = 'asteLastVisited';
    const stored = localStorage.getItem(key);
    const visits = stored ? JSON.parse(stored) : [];
    if (!visits.includes(idAsta)) {
        visits.push(idAsta);
        localStorage.setItem(key, JSON.stringify(visits));
    }

}


export function hideAllPages() {
    document.getElementById('vendoPage').hidden = true;
    document.getElementById('acquistoPage').hidden = true;
    document.getElementById('dettaglioAstaPage').hidden = true;
    document.getElementById('offertaPage').hidden = true;
}