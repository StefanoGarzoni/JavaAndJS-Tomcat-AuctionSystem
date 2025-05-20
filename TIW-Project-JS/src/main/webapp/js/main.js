import {renderAcquistoPage} from "./acquisto.js";
import {renderVendoPage} from "./vendo.js";

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

    if (lastActionIsAddedAsta()) {		// entra nel then SSE l'ultima azione è la creazione dell'asta
        showVendo();
    } else {
        showAcquisto();
    }

});

// Show "Vendo" pages
export function showVendo() {
    moveToAcquisto.removeAttribute('hidden');
    moveToVendo.setAttribute('hidden', true);
    hideAllPages();
    renderVendoPage();
}

// Show "Acquisto" page
export function showAcquisto() {
    moveToVendo.removeAttribute('hidden');
    moveToAcquisto.setAttribute('hidden', true);
    hideAllPages();
    renderAcquistoPage();
}

export function hideAllPages() {
    document.getElementById('vendoPage').hidden = true;
    document.getElementById('acquistoPage').hidden = true;
    document.getElementById('dettaglioAstaPage').hidden = true;
    document.getElementById('offertaPage').hidden = true;
    document.getElementById('back').hidden = true;
}

function lastActionIsAddedAsta() {
	const request = new XMLHttpRequest();
	request.open("POST", "/TIW-Project-JS/home");
	
	request.onreadystatechange = () => {
		if(request.readyState == 4){
			if(request.status == 200){
				const userLastActionWasAddedAsta = JSON.parse(request.responseText).userLastActionWasAddedAsta;
				
				return userLastActionWasAddedAsta;
			}
			else{
				alert("Problema con il caricamento dei dati dal server");
			}
		}
	}

}