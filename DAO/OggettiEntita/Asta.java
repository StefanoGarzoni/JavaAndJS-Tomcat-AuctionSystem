//Beans per le Aste

package DAO.OggettiEntita;
import java.sql.Date;
import java.sql.Time;

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
    public Date getData_scadenza() {
        return data_scadenza;
    }
    public Time getOra_scadenza() {
        return ora_scadenza;
    }
    public int getOfferta_max() {
        return offerta_max;
    }
    public boolean isChiusa() {
        return chiusa;
    }
}
