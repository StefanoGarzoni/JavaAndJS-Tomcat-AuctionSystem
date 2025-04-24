//Interfaccia Tabella Aste
package DAO;
import java.sql.Time;
import java.util.*;

import DAO.OggettiEntita.Asta;

import java.sql.Date;

public interface AsteDAO{
	void insertNewAsta(String usernameCreatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza);
	ArrayList<Asta> getAllClosedAsteInfoByCreator(String usernamaCreatore); 
	ArrayList<Asta> getAllOpenAsteInfoByCreator(String usernamaCreatore);
    ArrayList<Asta> getAsteByStringInArticoli(String stringaDiRicerca); 
    void setOffertaMax(int idAsta, int idOfferta);
    double getPrezzoOffertaMax(int idAsta);
    Asta getOpenAstaById(int idAsta);
    Asta getClosedAstaById(int idAsta);
    boolean astaCanBeClosed(int idAsta);
    void setAstaAsClosed(int idAsta, String username);
    Map<Asta, ArrayList<String>> getInfoFromAClosedAsta(int idAsta);
    boolean checkCreatorOfAsta(String username, int idAsta);

}