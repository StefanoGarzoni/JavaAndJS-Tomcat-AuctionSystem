//Interfaccia DAO Tabella Aste

package it.polimi.tiw.dao;

import java.sql.Time;
import java.util.*;
import it.polimi.tiw.dao.Beans.Asta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

public interface AsteDAO{
	int insertNewAsta(Connection conn, String usernameCreatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza) throws SQLException;
	ArrayList<Asta> getAllClosedAsteInfoByCreator(Connection conn, String usernamaCreatore) throws SQLException; 
	ArrayList<Asta> getAllOpenAsteInfoByCreator(Connection conn, String usernamaCreatore) throws SQLException;
    ArrayList<Asta> getAsteByStringInArticoli(Connection conn, String stringaDiRicerca, String username) throws SQLException; 
    void setOffertaMax(Connection conn, int idAsta, int idOfferta) throws SQLException;
    Map<Double,Double> getPrezzoOffertaMaxANDRialzoMinimo(Connection conn, int idAsta) throws SQLException;
    Asta getOpenAstaById(Connection conn, int idAsta) throws SQLException;
    Asta getClosedAstaById(Connection conn, int idAsta) throws SQLException;
    boolean astaCanBeClosed(Connection conn, int idAsta) throws SQLException;
    void setAstaAsClosed(Connection conn, int idAsta, String username) throws SQLException;
    Map<Asta, ArrayList<String>> getInfoFromAClosedAsta(Connection conn, int idAsta) throws SQLException;
    boolean checkCreatorOfAsta(Connection conn, String username, int idAsta) throws SQLException;
    Double getRialzoMinimo(Connection conn, int idAsta) throws SQLException;

}