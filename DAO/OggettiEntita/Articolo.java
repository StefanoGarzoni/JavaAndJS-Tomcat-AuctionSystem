//Beans per gli Articoli

package DAO.OggettiEntita;
/*
    cod INT PRIMARY KEY AUTO_INCREMENT,
    venditore VARCHAR(50),
    id_asta INT DEFAULT NULL,
    nome VARCHAR(100) NOT NULL,
    descrizione TEXT NOT NULL,
    img VARCHAR(255) NOT NULL,
    prezzo DECIMAL(10,2) NOT NULL,
    venduto BOOLEAN DEFAULT FALSE,
 */
public class Articolo {
    private int cod;
    private String nomeArticolo;
    private String descrizione;
    private String imgPath;
    private double prezzo;
    private String venditore;
    private boolean venduto;
    private int idAsta;
    
    public Articolo(int cod, String nomeArticolo, String descrizione, String imgPath, double prezzo, String venditore, boolean venduto, int idAsta) {
        this.cod = cod;
        this.nomeArticolo = nomeArticolo;
        this.descrizione = descrizione;
        this.imgPath = imgPath;
        this.prezzo = prezzo;
        this.venditore = venditore;
        this.venduto = venduto;
        this.idAsta = idAsta;
    }

    //GETTERS
    public int getCod() {
        return cod;
    }
    public String getNomeArticolo() {
        return nomeArticolo;
    }
    public String getDescrizione() {
        return descrizione;
    }
    public String getImgPath() {
        return imgPath;
    }
    public double getPrezzo() {
        return prezzo;
    }
    public String getVenditore() {
        return venditore;
    }
    public boolean isVenduto() {
        return venduto;
    }
    public int getIdAsta() {
        return idAsta;
    }

}
