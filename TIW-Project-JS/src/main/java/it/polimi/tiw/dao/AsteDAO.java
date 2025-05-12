//Interfaccia DAO Tabella Aste

package it.polimi.tiw.dao;

import java.sql.Time;
import java.util.*;
import it.polimi.tiw.dao.Beans.Asta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

public interface AsteDAO{
	Asta insertNewAsta(Connection conn, String usernameCreatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza) throws SQLException;
	ArrayList<Asta> getAllClosedAsteInfoByCreator(Connection conn, String usernamaCreatore); 
	ArrayList<Asta> getAllOpenAsteInfoByCreator(Connection conn, String usernamaCreatore);
    ArrayList<Asta> getAsteByStringInArticoli(Connection conn, String stringaDiRicerca, String username); 
    void setOffertaMax(Connection conn, int idAsta, int idOfferta);
    Map<Double,Double> getPrezzoOffertaMaxANDRialzoMinimo(Connection conn, int idAsta);
    Asta getOpenAstaById(Connection conn, int idAsta);
    Asta getClosedAstaById(Connection conn, int idAsta);
    boolean astaCanBeClosed(Connection conn, int idAsta);
    void setAstaAsClosed(Connection conn, int idAsta, String username);
    Map<Asta, ArrayList<String>> getInfoFromAClosedAsta(Connection conn, int idAsta);
    boolean checkCreatorOfAsta(Connection conn, String username, int idAsta);
    public Double getRialzoMinimo(Connection conn, int idAsta);

}