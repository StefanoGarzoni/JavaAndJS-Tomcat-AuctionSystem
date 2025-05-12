import { renderVendoPage, setupPageVendo} from './vendo.js';
import { setupPageAscquisto } from './acquisto.js';

// aggiungo gli event listeners
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

    // reindirizzamento in base all'ultima azione svolta dall'utente
    const lastAction = getCookie("lastAction");
    if (lastAction === 'addedAsta') {
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
		setCookie(cookieName, "true", 30);
	}
}

export function setCookie(name, value, days) {
    const expiration = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString(); 		// calcolo scadenza a days giorni di distanza
    document.cookie = `${name}=${encodeURIComponent(JSON.stringify(value))};expires=${expiration};path=/`;
}

export function getCookie(name) {
    const cookies = document.cookie.split(';');	// divide tutti i cookies
    
	for (const cookie of cookies) {
        const [key, value] = cookie.trim().split('=');	// divide il nome e il valore del cookies
        if (key === name) {
			return JSON.parse(decodeURIComponent(value));	// restituisce il valore corrente del cookie richiesto, come oggetto JSON
        }
    }
    return null;
}

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

export function hideAllPages() {
    document.getElementById('vendoPage').hidden = true;
    document.getElementById('acquistoPage').hidden = true;
    document.getElementById('dettaglioAstaPage').hidden = true;
    document.getElementById('offertaPage').hidden = true;
}