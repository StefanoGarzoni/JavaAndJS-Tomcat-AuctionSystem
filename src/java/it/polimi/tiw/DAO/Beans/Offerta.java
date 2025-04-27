//Beans per le Offerte

package DAO.Beans;
import java.sql.Date;
import java.sql.Time;
/*
    CREATE TABLE Offerte (
    id_offerta INT PRIMARY KEY AUTO_INCREMENT,
    utente VARCHAR(50) NOT NULL,
    id_asta INT NOT NULL,
    prezzo DECIMAL(10000,2) NOT NULL,
    data_offerta DATE NOT NULL,
    ora_offerta TIME NOT NULL,
    FOREIGN KEY (utente) REFERENCES Utenti(username),
    FOREIGN KEY (id_asta) REFERENCES Aste(id_asta)

 */
public class Offerta {
    private int idOfferta;
    private String utente;
    private int idAsta;
    private double prezzo;
    private Date dataOfferta;
    private Time oraOfferta;

    public Offerta(int idOfferta, String utente, int idAsta, double prezzo, Date dataOfferta, Time oraOfferta) {
        this.idOfferta = idOfferta;
        this.utente = utente;
        this.idAsta = idAsta;
        this.prezzo = prezzo;
        this.dataOfferta = dataOfferta;
        this.oraOfferta = oraOfferta;
    }

    //GETTERS
    public int getIdOfferta() {
        return idOfferta;
    }
    public String getUtente() {
        return utente;
    }
    public int getIdAsta() {
        return idAsta;
    }
    public double getPrezzo() {
        return prezzo;
    }
    public Date getDataOfferta() {
        return dataOfferta;
    }
    public Time getOraOfferta() {
        return oraOfferta;
    }
    
}
