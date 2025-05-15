//Beans per le Aste

package it.polimi.tiw.dao.Beans;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

/*
    id_asta INT PRIMARY KEY AUTO_INCREMENT,
    creatore VARCHAR(50),
    prezzoIniziale DECIMAL(10, 2) NOT NULL,
    rialzoMinimo DECIMAL(10, 2) NOT NULL,
    dataScadenza DATE NOT NULL,
    oraScadenza TIME NOT NULL,
    offertaMax INT DEFAULT NULL,
    chiusa BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (creatore) REFERENCES Utenti(username),
    FOREIGN KEY (offertaMax) REFERENCES Offerte(id_offerta)

 */
public class Asta {
    private int idAsta;
    private String creatore;
    private double prezzoIniziale;
    private double rialzoMinimo;
    private Date dataScadenza;
    private Time oraScadenza;
    private int offertaMax;
    private boolean chiusa;
    
    private ArrayList<Articolo> articoli;
    private int giorniRimanenti;
    private int oreRimanenti;
    
    public Asta(int idAsta, String creatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza, int offertaMax, boolean chiusa, ArrayList<Articolo> articoli) {
        this.idAsta = idAsta;
        this.creatore = creatore;
        this.prezzoIniziale = prezzoIniziale;
        this.rialzoMinimo = rialzoMinimo;
        this.dataScadenza = dataScadenza;
        this.oraScadenza = oraScadenza;
        this.offertaMax = offertaMax;
        this.chiusa = chiusa;
        this.articoli = articoli;
    }
    
    // SETTERS
    public void setGiorniRimanenti(int giorniRimanenti) {
    	this.giorniRimanenti = giorniRimanenti;
    }
    public void setOreRimanenti(int oreRimanenti) {
    	this.oreRimanenti = oreRimanenti;
    }

    //GETTERS
    public int getIdAsta() {
        return idAsta;
    }
    public String getCreatore() {
        return creatore;
    }
    public double getPrezzoIniziale() {
        return prezzoIniziale;
    }
    public double getRialzoMinimo() {
        return rialzoMinimo;
    }
    public Date getDataScadenza() {
        return dataScadenza;
    }
    public Time getOraScadenza() {
        return oraScadenza;
    }
    public int getGiorniRimanenti() {
    	return giorniRimanenti;
    }
    public int getOreRimanenti() {
    	return oreRimanenti;
    }
    public int getOffertaMax() {
        return offertaMax;
    }
    public boolean isChiusa() {
        return chiusa;
    }
    public ArrayList<Articolo> getArticoli(){
    	return articoli;
    }
}
