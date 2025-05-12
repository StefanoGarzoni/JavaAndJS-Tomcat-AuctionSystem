import { renderDettaglioAstaPage } from "./dettaglioAsta";
import { setCookie } from "./main";

function setupPageVendo(){
	const vendoSection = document.querySelector("#vendoPage");
	vendoSection.removeAttribute("hidden");
	
	// aggiunta gestione eventi creazione articolo e asta
	document.querySelector("#submitNewArticolo").addEventListener(
		"click",
		newArticolo
	);
	
	document.querySelector("#submitNewAsta").addEventListener(
		"click",
		newAsta
	);
}

/*
export function freePageVendo(){
	document.querySelector("#vendoPage").setAttribute("hidden");
	
	// rimozione degli event listeners
	document.querySelector("#submitNewArticolo").removeEventListener(
		"click",
		newArticolo
	);
	
	document.querySelector("#submitNewAsta").removeEventListener(
		"click",
		newAsta
	);
	
	// rimozione degli elementi inseriti precedentemente
	document.querySelector("#bodyTabellaAsteAperte").innerHTML = '';
	document.querySelector("#bodyTabellaAsteChiuse").innerHTML = '';
	document.querySelector("#bodyTabellaArticoliNewAsta").innerHTML = '';
	
	document.querySelector("#newAstaMessage").textContent = '';
	document.querySelector("#newArticoloMessage").textContent = '';
}*/

export function renderVendoPage(){
	const request = new XMLHttpRequest();
	request.open("GET", "/TIW-Project-JS/vendo");
	
	request.onreadystatechange = () => { showVendoContent(request); };
	request.send();
	
	setupPageVendo();
}

function showVendoContent(request){
	if(request.readyState == 4 && request.status == 200){
		const vendoContent = JSON.parse(request.responseText);
		
		const openAste = vendoContent.openAste;
		const closedAste = vendoContent.closedAste;
		const articoli = vendoContent.articoli;
		
		// aggiorno solo le sezioni che hanno avuto modifiche
		// se gli array con gli elementi sono vuoti => non ci sono state modifiche
		if(openAste.length > 0){
			openAste.forEach( (currentAsta) => {
				addOpenAstaInTable(currentAsta);
			});
		}
			
		if(closedAste.length > 0){
			closedAste.forEach((currentAsta) => {
				addClosedAstaInTable(currentAsta);
			});			
		}
			
		if(articoli.length > 0){
			articoli.forEach((articolo) => {
				addArticoloInTable(articolo);
			});
		}
	}
	else if (request.status == 505) {
		alert("Errore "+request.status+" durante il caricamento della pagina");
	}
}

function addOpenAstaInTable(asta){
	const tbody = document.querySelector("#bodyTabellaAsteAperte");
	
	const template = document.querySelector("#astaApertaRow");
	const newRow = template.content.clone(true);
	
	let idAstaElement = document.createElement("td");
	idAstaElement.text = asta.idAsta;
	newRow.appendChild(idAstaElement);
	
	let prezzoInizialeElement = document.createElement("td");
	prezzoInizialeElement.text = asta.prezzo_iniziale;
	newRow.appendChild(prezzoInizialeElement);
	
	let offertaMaxElement = document.createElement("td");
	offertaMaxElement.text = asta.offerta_max;
	newRow.appendChild(offertaMaxElement);
	
	let dataScadenzaElement = document.createElement("td");
	dataScadenzaElement.text = asta.data_scadenza;
	newRow.appendChild(dataScadenzaElement);
	
	let oraScadenzaElement = document.createElement("td");
	oraScadenzaElement.text = asta.ora_scadenza;
	newRow.appendChild(oraScadenzaElement);
			
	let giorniRimanentiElement = document.createElement("td");
	giorniRimanentiElement.text = asta.giorni_rimanenti;
	newRow.appendChild(giorniRimanentiElement);

	let oreRimanentiElement = document.createElement("td");
	oreRimanentiElement.text = asta.ore_rimanenti;
	newRow.appendChild(oreRimanentiElement);
	
	// creazione tabella articoli
	let articlesTable = document.createElement("table");
	
	asta.articoli.forEach((articolo) => {
		let tr = document.createElement("tr");
		
		let codiceTd = document.createElement("td");
		codiceTd.textContent = articolo.cod;
		tr.appendChild(codiceTd);
		
		let nomeTd = document.createElement("td");
		nomeTd.textContent = articolo.nomeArticolo;
		tr.appendChild(nomeTd);
		
		let prezzoTd = document.createElement("td");
		prezzoTd.textContent = articolo.nomeArticolo;
		tr.appendChild(prezzoTd);
		
		articlesTable.appendChild(tr);
	});
	
	let articlesTableTd = document.createElement("td");
	articlesTableTd.appendChild(articlesTable);
	newRow.appendChild(articlesTableTd);
	
	// creazione bottone per passare al dettaglio asta
	let dettaglioAstaButton = document.createElement("button");
	dettaglioAstaButton.addEventListener("click", (idAsta) => {
		renderDettaglioAstaPage(idAsta);
	});
	dettaglioAstaButton.textContent = "Dettaglio Asta";
	newRow.appendChild(dettaglioAstaButton);
	
	// inserisco la nuova riga nella tabella delle aste aperte
	tbody.appendChild(newRow);
}

function addClosedAstaInTable(asta){
	const tbody = document.querySelector("#bodyTabellaAsteChiuse");
	
	const template = document.querySelector("#astaChiusaRow");
	const newRow = template.content.clone(true);
	
	let idAstaElement = document.createElement("td");
	idAstaElement.textContent = asta.idAsta;
	newRow.appendChild(idAstaElement);
	
	let prezzoInizialeElement = document.createElement("td");
	prezzoInizialeElement.textContent = asta.prezzoIniziale;
	newRow.appendChild(prezzoInizialeElement);
	
	let offertaMaxElement = document.createElement("td");
	offertaMaxElement.textContent = asta.idAsta;
	newRow.appendChild(offertaMaxElement);
	
	let dataScadenzaElement = document.createElement("td");
	dataScadenzaElement.textContent = asta.idAsta;
	newRow.appendChild(dataScadenzaElement);
	
	let oraScadenzaElement = document.createElement("td");
	oraScadenzaElement.textContent = asta.idAsta;
	newRow.appendChild(oraScadenzaElement);
				
	tbody.appendChild(newRow);
}


function addArticoloInTable(articolo){
	const tbody = document.querySelector("#bodyTabellaArticoliNewAsta");
	
	const template = document.querySelector("#astaChiusaRow");
	const newRow = template.content.clone(true);
	
	// imposta il value nella checkbox dell'articolo
	newRow.querySelector('input[name="codiceArticolo"]').value = articolo.cod;
	
	let codiceArticoloElement = document.createElement("td");
	codiceArticoloElement.text = articolo.cod;
	newRow.appendChild(codiceArticoloElement);
	
	let nomeArticoloElement = document.createElement("td");
	nomeArticoloElement.text = articolo.nomeArticolo;
	newRow.appendChild(nomeArticoloElement);
	
	let descrizioneArticoloElement = document.createElement("td");
	descrizioneArticoloElement.text = articolo.descrizione;
	newRow.appendChild(descrizioneArticoloElement);
	
	let prezzoArticoloElement = document.createElement("td");
	prezzoArticoloElement.text = articolo.prezzo;
	newRow.appendChild(prezzoArticoloElement);
	
	tbody.appendChild(newRow);
}

function newArticolo(){	// e è l'evento che ha causato la chiamata della callback
	const nome = document.querySelector("#nomeNewArticolo").value;
	const descrizione = document.querySelector("#descrizioneNewArticolo").value;
	const immagine = document.querySelector("#immagineNewArticolo").files[0];
	const prezzo = parseFloat(document.querySelector("#prezzoNewArticolo").value);
	
	if(!nome || !descrizione || !immagine || isNaN(prezzo)){
		document.querySelector("#newArticoloMessage").textContent = "Alcuni parametri non sono stati inseriti";
		return;
	}
	
	document.querySelector("#newArticoloMessage").textContent = "";	// elimino un eventuale errore precedente
	
	const formData = new FormData();
	formData.append("nome", nome);
	formData.append("descrizione", descrizione);
	formData.append("immagine", immagine);
	formData.append("prezzo", prezzo);
	
	const request = new XMLHttpRequest;
	request.open("POST", "/TIW-Project-JS/newArticolo");
	
	request.onreadystatechange = () => {
		if(request.readyState == 4){
			if(request.status == 200){
				const newArticoloInserito = JSON.parse(request.responseText);
				
				addArticoloInTable(newArticoloInserito);
				
				setCookie("lastAction", "addedArticolo", 30);
			}
			else{
				document.querySelector("#newArticoloMessage").textContent = "Problema con l'aggiunta dell'articolo"
			}
		}
	}
	
	request.send(formData);
}

function newAsta(){
	const rialzoMinimo = parseFloat(document.querySelector("#rialzoMinimoNewAsta").value);
	const dataScadenzaNewAsta = document.querySelector("#dataScadenzaNewAsta").value;
	const oraScadenzaNewAsta = document.querySelector("#oraScadenzaNewAsta").value;

	const checkboxes = document.querySelectorAll('input[type="checkbox"][name="codiceArticolo"]:checked');
	const codiciArticoli = Array.from(checkboxes).map(cb => cb.value);
	
	if(!isNaN(rialzoMinimo) || !dataScadenzaNewAsta || !oraScadenzaNewAsta || codiciArticoli.length == 0){
		document.querySelector("#newAstaMessage").textContent = "Alcuni parametri non sono stati inseriti";
		return;
	}
	
	document.querySelector("#newAstaMessage").textContent = "";	// elimino un eventuale errore precedente
	
	const dateTimeString = `${dateInput}T${timeInput}:00`;	// formato per creare un oggetto date
    const selectedDateTime = new Date(dateTimeString);
	
	if(selectedDateTime < new Date()){
		document.querySelector("#newAstaMessage").textContent = "La scadenza scelta è nel passato, inserire una scadenza valida";
		return;
	}
	
	document.querySelector("#newAstaMessage").textContent = "";	// elimino un eventuale errore precedente
	
	const formData = new FormData();
	formData.append("codiceArticolo", codiciArticoli);
	formData.append("rialzoMinimo", rialzoMinimo);
	formData.append("dataScadenza", dataScadenzaNewAsta);
	formData.append("oraScadenza", oraScadenzaNewAsta);
	
	const request = new XMLHttpRequest;
	request.open("POST", "/TIW-Project-JS/newArticolo");
	
	request.onreadystatechange = () => {
		if(request.readyState == 4){
			if(request.status == 200){
				const newAstaInserita = JSON.parse(request.responseText);
				
				addOpenAstaInTable(newAstaInserita);
				
				codiciArticoli.forEach((codiceArticolo) => {
					removeArticoloFromTable(codiceArticolo);					
				});
				
				setCookie("lastAction", "addedAsta", 30);
			}
			else{
				document.querySelector("#newAstaMessage").textContent = "Il server ha incontrato un problema durante l'aggiunta dell'articolo";
			}
		}
	}
	
	request.send(formData);
}

function removeArticoloFromTable(codiceArticolo){
  const tbody = document.getElementById("bodyTabellaAsteByKeyword");
  const rows = tbody.getElementsByTagName("tr");

  for (let row of rows) {
    const firstCell = row.cells[0]; // l'id asta è nel primo td
    if (firstCell && firstCell.textContent === codiceArticolo.toString()) {		// entra se il text content è uguale all'id dell'articolo da eliminare
      tbody.removeChild(row);
      break; // rimuovo solo la prima riga (poichè solo una riga ha l'id specificato)
    }
  }
}