export function render(){
	document.querySelector("#acquistoPage").removeAttribute("hidden");
	
	document.querySelector("#sumbitSearchByKeyword").addEventListener(
		"click",
		searchAstaByKeyword
	);
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
				orEach((asta) => {
					addAstaInTable(asta);
				});
			}
			else{
				document.querySelector("#listaAsteByKeywordMessage").textContent = "Nessuna asta con la parola chiave scelta";
			}
		}
		else{
			document.querySelector("#listaAsteByKeywordMessage").textContent = "Errore interno al serve";
		}
	}
}

function addAstaInTable(asta){
	const tbody = document.querySelector("#bodyTabellaAsteByKeyword");
	
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
	dettaglioAstaButton.classList.add("dettaglioAstaApertaButton");
	dettaglioAstaButton.value = asta.idAsta
	newRow.appendChild(dettaglioAstaButton);
	
	// inserisco la nuova riga nella tabella delle aste aperte
	tbody.appendChild(newRow);
}