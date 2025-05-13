import { renderDettaglioAstaPage } from "./dettaglioAsta";
import { setCookie, getCookie } from "./main";

export function renderAcquistoPage(){	
	document.querySelector("#sumbitSearchByKeyword").addEventListener(
		"click",
		searchAstaByKeyword
	);
	
	if(getCookie("renderTableAsteVisionate").value == true){
		document.querySelector("#bodyTabellaAsteVisionate").innerHTML = "";		// rimuovo la lista precedente
		renderAsteVisionate();
	}
}

function renderAsteVisionate(){
	const asteVisionate = getCookie("asteVisionate");
	
	if(asteVisionate.length > 0){
		document.querySelector("#listaAsteVisionate").removeAttribute("hidden");
		
		let formData = new FormData();
		formData.append("asteVisionate", asteVisionate);
		
		// richiedi al server le aste visionate
		let request = new XMLHttpRequest();
		request.open("GET", "/TIW-project/asteVisionate");
		
		request.onreadystatechange = (request) => {
			if(request.readyState == 4){
				if(request.status == 200){
					const asteVisionate = JSON.parse(request.responseText);
					
					for(const asta of asteVisionate){
						addAstaInTable(asta, "bodyTabellaAsteVisionate");
					}
				}
				else
					document.querySelector("#listaAsteVisionateMessage").textContent = "Errore interno al server";
			}
		}
		
		request.send(formData);
	}
}

export function freePageAcquisto(){
	document.querySelector("#acquistoPage").setAttribute("hidden");
	
	document.querySelector("#sumbitSearchByKeyword").removeEventListener(
		"click",
		searchAstaByKeyword
	);
	
	document.querySelector("#listaAsteByKeyword").setAttribute("hidden");	// nascondo la lista di aste
	document.querySelector("#bodyTabellaAsteByKeyword").innerHTML = "";		// svuoto la tabella delle aste con la parola chiave precedentemente ricercata
																			// in questo modo vengono eliminati anche gli event listeners sui bottoni																
}

function searchAstaByKeyword(){
	const keyword = document.querySelector("#parolaChiave").value;
	
	if(!keyword){
		document.querySelector("#listaAsteByKeywordMessage").textContent = "Parola chiave mancante";
		return;
	}
	
	document.querySelector("#listaAsteByKeywordMessage").textContent = "";
	
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
			document.querySelector("#listaAsteByKeyword").removeAttribute("hidden");
			document.querySelector("#bodyTabellaAsteByKeyword").innerHTML = "";		// svuoto la tabella delle aste con la parola chiave precedentemente ricercata
			
			const aste = JSON.parse(request.responseText);
			
			if(aste.length > 0){
				aste.forEach((asta) => {
					addAstaInTable(asta, "#bodyTabellaAsteByKeyword");
				});
				setCookie("lastAction", {"value":"addedAsta"}, 30);
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
	const newRow = template.content.clone(true);
	
	let idAstaElement = document.createElement("td");
	idAstaElement.text = asta.idAsta;
	newRow.appendChild(idAstaElement);
	
	let creatoreElement = document.createElement("td");
	creatoreElement.text = asta.creatore;
	newRow.appendChild(creatoreElement);
	
	let prezzoInizialeElement = document.createElement("td");
	prezzoInizialeElement.text = asta.prezzo_iniziale;
	newRow.appendChild(prezzoInizialeElement);
	
	let rialzoMinimoElement = document.createElement("td");
	rialzoMinimoElement.text = asta.rialzo_minimo;
	newRow.appendChild(rialzoMinimoElement);
		
	let dataScadenzaElement = document.createElement("td");
	dataScadenzaElement.text = asta.data_scadenza;
	newRow.appendChild(dataScadenzaElement);
	
	let oraScadenzaElement = document.createElement("td");
	oraScadenzaElement.text = asta.ora_scadenza;
	newRow.appendChild(oraScadenzaElement);
	
	let offertaMaxElement = document.createElement("td");
	offertaMaxElement.text = asta.offerta_max;
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
	dettaglioAstaButton.addEventListener((idAsta) => {
		addAstaVisionata(idAsta);	// aggiungo l'asta tra quelle visionate dall'utente
		setCookie("renderTableAsteVisionate",  {"value" : true}, 30);	// la prossima volta acquisto dovrà ri-renderizzare la tabella delle pagine da renderizzare
		renderDettaglioAstaPage(idAsta);
	});
	newRow.appendChild(dettaglioAstaButton);
	
	// inserisco la nuova riga nella tabella delle aste aperte
	tbody.appendChild(newRow);
}

// funzione che aggiunge un'asta tra quelle visionate
function addAstaVisionata(idAsta) {
    let aste = getCookie('asteVisionate') || [];
    
	if (!aste.includes(idAsta)) {
        aste.push(idAsta);		// aggiunge l'asta alla lista JSON delle aste visionate
        setCookie('asteVisionate', aste, 30);
    }
}