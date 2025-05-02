//Beans per le Aste

package it.polimi.tiw.dao.Beans;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

/*
    id_asta INT PRIMARY KEY AUTO_INCREMENT,
    creatore VARCHAR(50),
    prezzo_iniziale DECIMAL(10, 2) NOT NULL,
    rialzo_minimo DECIMAL(10, 2) NOT NULL,
    data_scadenza DATE NOT NULL,
    ora_scadenza TIME NOT NULL,
    offerta_max INT DEFAULT NULL,
    chiusa BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (creatore) REFERENCES Utenti(username),
    FOREIGN KEY (offerta_max) REFERENCES Offerte(id_offerta)

 */
public class Asta {
    private int idAsta;
    private String creatore;
    private double prezzo_iniziale;
    private double rialzo_minimo;
    private Date data_scadenza;
    private Time ora_scadenza;
    private int offerta_max;
    private boolean chiusa;
    
    private ArrayList<Articolo> articoli;
    private int giorni_rimanenti;
    private int ore_rimanenti;
    
    public Asta(int idAsta, String creatore, double prezzo_iniziale, double rialzo_minimo, Date data_scadenza, Time ora_scadenza, int offerta_max, boolean chiusa) {
        this.idAsta = idAsta;
        this.creatore = creatore;
        this.prezzo_iniziale = prezzo_iniziale;
        this.rialzo_minimo = rialzo_minimo;
        this.data_scadenza = data_scadenza;
        this.ora_scadenza = ora_scadenza;
        this.offerta_max = offerta_max;
        this.chiusa = chiusa;
    }
    
    public Asta(int idAsta, Date data_scadenza, Time ora_scadenza, int offerta_max, ArrayList<Articolo> articoli) {
        this.idAsta = idAsta;
        this.data_scadenza = data_scadenza;
        this.ora_scadenza = ora_scadenza;
        this.offerta_max = offerta_max;
        this.articoli = articoli;
    }
    
    // SETTERS
    public void setGiorniRimanenti(int giorniRimanenti) {
    	this.giorni_rimanenti = giorniRimanenti;
    }
    public void setOreRimanenti(int oreRimanenti) {
    	this.ore_rimanenti = oreRimanenti;
    }

    //GETTERS
    public int getIdAsta() {
        return idAsta;
    }
    public String getCreatore() {
        return creatore;
    }
    public double getPrezzo_iniziale() {
        return prezzo_iniziale;
    }
    public double getRialzo_minimo() {
        return rialzo_minimo;
    }
    public Date getDataScadenza() {
        return data_scadenza;
    }
    public Time getOraScadenza() {
        return ora_scadenza;
    }
    public int getGiorniRimanenti() {
    	return giorni_rimanenti;
    }
    public int getOreRimanenti() {
    	return ore_rimanenti;
    }
    public int getOffertaMax() {
        return offerta_max;
    }
    public boolean isChiusa() {
        return chiusa;
    }
    public ArrayList<Articolo> getArticoli(){
    	return articoli;
    }
}
