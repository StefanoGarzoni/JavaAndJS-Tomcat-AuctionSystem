import { setupPageVendo} from './vendo.js';
import { setupPageAscquisto } from './acquisto.js';

//"Menu"
document.addEventListener('DOMContentLoaded', () => {
    const moveToVendo = document.getElementById('moveToVendo');
    const moveToAcquisto = document.getElementById('moveToAcquisto');

    moveToVendo.addEventListener('click', () => {
        showVendo();
    });
    moveToAcquisto.addEventListener('click', () => {
        showAcquisto();
    });


    if (getCookie('lastAction') === 'addedAsta') {
        showVendo();
    } else {
        showAcquisto();
    }

});

//NOTA DOVREBBE ESSERE RIMOSSO E GESTITO TUTTO NELLE SERVLET
//--------------------------------------------------------------------------------------------
//CREAZIONE INIZIALE DEI COOKIE

// Dichiaro i tre nomi dei cookie
var cookieNames = ['renderTableAsteAperte', 'renderAllTablesAste', 'renderTableAsteVisionate'];

//scadenza di una settimana
var oneWeek = 7 * 24 * 60 * 60;

//scorro tutti i cookie
for (var i = 0; i < cookieNames.length; i++) {
    if(cookieNames[i] === 'renderTableAsteVisionate') {
        // Se il cookie è renderTableAsteVisionate, lo imposto a false
        document.cookie = 
            cookieNames[i] + '=' + encodeURIComponent("False") +
            '; max-age=' + oneWeek +
            '; path=/';
    }else{
        document.cookie = 
        cookieNames[i] + '=' + encodeURIComponent("True") +
        '; max-age=' + oneWeek +
        '; path=/';
    }

}
//--------------------------------------------------------------------------------------------


// Show "Vendo" pages
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