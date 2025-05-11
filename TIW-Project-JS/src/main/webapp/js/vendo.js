function setup(){
	const vendoSection = document.querySelector("#vendoPage");
	vendoSection.removeAttribute("hidden");
	
	requireVendoContent();
	
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

function freePage(){
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
}

function requireVendoContent(){
	const request = new XMLHttpRequest();
	request.open("GET", "/TIW-Project-JS/vendo");
	
	request.onreadystatechange = () => { showVendoContent(request); };
	request.send();
}

function showVendoContent(request){
	if(request.readyState == 4 && request.status == 200){
		const vendoContent = JSON.parse(request.responseText);
		
		const openAste = vendoContent.openAste;
		const closedAste = vendoContent.closedAste;
		const articoli = vendoContent.articoli;
		
		openAste.forEach( (currentAsta) => {
			addOpenAstaInTable(currentAsta);
		});
		
		closedAste.forEach((currentAsta) => {
			addClosedAstaInTable(currentAsta);
		});
		
		articoli.forEach((articolo) => {
			addArticoloInTable(articolo);
		});
	}
	else if (request.status == 505) {
		// mostra errore 505
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
	prezzoInizialeElement.text = asta.prezzoIniziale;
	newRow.appendChild(prezzoInizialeElement);
		
	let offertaMaxElement = document.createElement("td");
	offertaMaxElement.text = asta.offertaMax;
	newRow.appendChild(offertaMaxElement);
		
	let dataScadenzaElement = document.createElement("td");
	dataScadenzaElement.text = asta.dataScadenza;
	newRow.appendChild(dataScadenzaElement);
			
	let oraScadenzaElement = document.createElement("td");
	oraScadenzaElement.text = asta.oraScadenza;
	newRow.appendChild(oraScadenzaElement);
			
	let giorniRimanentiElement = document.createElement("td");
	giorniRimanentiElement.text = asta.giorniRimanenti;
	newRow.appendChild(giorniRimanentiElement);

	let oreRimanentiElement = document.createElement("td");
	oreRimanentiElement.text = asta.oreRimanenti;
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
	dettaglioAstaButton.textContent = "Dettaglio Asta";
	dettaglioAstaButton.classList.add("dettaglioAstaApertaButton");
	dettaglioAstaButton.value = asta.idAsta
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
				const articoloInserito = JSON.parse(request.responseText).id;
				addArticoloInTable(articoloInserito);
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
				const astaInserita = JSON.parse(request.responseText);
				addOpenAstaInTable(astaInserita);
			}
			else{
				document.querySelector("#newAstaMessage").textContent = "Il server ha incontrato un problema durante l'aggiunta dell'articolo";
			}
		}
	}
	
	request.send(formData);
}