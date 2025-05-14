import { renderDettaglioAstaPage } from "./dettaglioAsta.js";
import { renderOffertaPage } from "./offerte.js";

export function renderAcquistoPage(){	
	document.querySelector("#acquistoPage").removeAttribute("hidden");
	
	document.querySelector("#sumbitSearchByKeyword").addEventListener(
		"click",
		searchAstaByKeyword
	);
	
	renderAsteVisionate();
}

function renderAsteVisionate(){
	// richiedi al server le aste visionate (la servlet analizzerà la lista di cookie e restituirà la lista delle rispettive aste)
	let request = new XMLHttpRequest();
	request.open("GET", "/TIW-Project-JS/acquisto");
	
	request.onreadystatechange = (request) => {
		if(request.readyState == 4){
			if(request.status == 200){
				// se è presente del contenuto nella risposta => sono le aste visitate da mostrare
					document.querySelector("#bodyTabellaAsteVisionate").innerHTML = '';	// svuoto la tabella precedente per far spazio ai dati aggiornati
					const asteVisionate = JSON.parse(request.responseText);
				
				if(asteVisionate.length > 0){
					document.querySelector("#listaAsteVisionate").removeAttribute("hidden");
					
					for(const asta of asteVisionate){
						addAstaInTable(asta, "bodyTabellaAsteVisionate");
					}
				}
			}
			else
				document.querySelector("#listaAsteVisionateMessage").textContent = "Errore interno al server";
		}
	}
	
	request.send();
}

export function freePageAcquisto(){
	document.querySelector("#acquistoPage").setAttribute("hidden");
	
	document.querySelector("#sumbitSearchByKeyword").removeEventListener(
		"click",
		searchAstaByKeyword
	);
	
	document.querySelector("#listaAsteByKeyword").hidden = true;	// nascondo la lista di aste
	document.querySelector("#bodyTabellaAsteByKeyword").innerHTML = "";		// svuoto la tabella delle aste con la parola chiave precedentemente ricercata
	document.querySelector("#listaAsteVisionate").hidden = true;																		// in questo modo vengono eliminati anche gli event listeners sui bottoni																
}

function searchAstaByKeyword(){
	const keyword = document.querySelector("#parolaChiave").value;
	
	if(!keyword){
		document.querySelector("#listaAsteByKeywordMessage").textContent = "Parola chiave mancante";
		return;
	}
	
	document.querySelector("#listaAsteByKeywordMessage").textContent = "";
	document.querySelector("#listaAsteByKeyword").hidden = true;
	
	// creazione parametri da passare con la richiesta
	const formData = new FormData();
	formData.append("parolaChiave", keyword);
	
	// creazione richiesta
	const request = new XMLHttpRequest();
	request.open("POST", "/TIW-Project-JS/acquisto");
	
	request.onreadystatechange = () => {
		showAsteByKeyword(request);
	};
	
	request.send(formData);
}

function showAsteByKeyword(request){
	if(request.readyState == 4){
		if(request.status == 200){
			document.querySelector("#bodyTabellaAsteByKeyword").innerHTML = "";		// svuoto la tabella delle aste con la parola chiave precedentemente ricercata
			
			const aste = JSON.parse(request.responseText);
			
			if(aste.length > 0){
				document.querySelector("#listaAsteByKeyword").removeAttribute("hidden");
				aste.forEach((asta) => {
					addAstaInTable(asta, "#bodyTabellaAsteByKeyword");
				});
			}
			else{
				document.querySelector("#listaAsteByKeywordMessage").textContent = "Nessuna asta con la parola chiave scelta";
			}
		}
		else{
			document.querySelector("#listaAsteByKeywordMessage").textContent = "Errore interno al server";
		}
	}
}

function addAstaInTable(asta, tableBodyQuerySelector){
	const tbody = document.querySelector(tableBodyQuerySelector);
	
	const template = document.querySelector("#astaByKeywordRow");
	const newRow = template.content.cloneNode(true);
	
	let idAstaElement = document.createElement("td");
	idAstaElement.textContent = asta.idAsta;
	newRow.appendChild(idAstaElement);
	
	let creatoreElement = document.createElement("td");
	creatoreElement.textContent = asta.creatore;
	newRow.appendChild(creatoreElement);
	
	let prezzoInizialeElement = document.createElement("td");
	prezzoInizialeElement.textContent = asta.prezzo_iniziale;
	newRow.appendChild(prezzoInizialeElement);
	
	let rialzoMinimoElement = document.createElement("td");
	rialzoMinimoElement.textContent = asta.rialzo_minimo;
	newRow.appendChild(rialzoMinimoElement);
		
	let dataScadenzaElement = document.createElement("td");
	dataScadenzaElement.textContent = asta.data_scadenza;
	newRow.appendChild(dataScadenzaElement);
	
	let oraScadenzaElement = document.createElement("td");
	oraScadenzaElement.textContent = asta.ora_scadenza;
	newRow.appendChild(oraScadenzaElement);
	
	let offertaMaxElement = document.createElement("td");
	offertaMaxElement.textContent = asta.offerta_max;
	newRow.appendChild(offertaMaxElement);
	
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
		
		articlesTable.appendChild(tr);
	});
	
	let articlesTableTd = document.createElement("td");
	articlesTableTd.appendChild(articlesTable);
	newRow.appendChild(articlesTableTd);
	
	// creazione bottone per passare al dettaglio asta
	let dettaglioAstaButton = document.createElement("button");
	
	// aggiungo l'event listener sul bottone
	dettaglioAstaButton.addEventListener("click", () => {
		renderOffertaPage(asta.idAsta);
	});
	
	dettaglioAstaButton.textContent = "Dettaglio Asta";
	dettaglioAstaButton.addEventListener("click", (idAsta) => {
		renderDettaglioAstaPage(idAsta);
	});
	newRow.appendChild(dettaglioAstaButton);
	
	// inserisco la nuova riga nella tabella delle aste aperte
	tbody.appendChild(newRow);
}