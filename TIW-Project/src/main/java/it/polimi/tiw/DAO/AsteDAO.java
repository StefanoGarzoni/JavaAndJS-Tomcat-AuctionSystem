//Interfaccia DAO Tabella Aste

package it.polimi.tiw.DAO;
import it.polimi.tiw.DAO.Beans.Asta;
import java.sql.Time;
import java.util.*;
import java.sql.Connection;
import java.sql.Date;

public interface AsteDAO{
	void insertNewAsta(Connection conn, String usernameCreatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza);
	ArrayList<Asta> getAllClosedAsteInfoByCreator(Connection conn, String usernamaCreatore); 
	ArrayList<Asta> getAllOpenAsteInfoByCreator(Connection conn, String usernamaCreatore);
    ArrayList<Asta> getAsteByStringInArticoli(Connection conn, String stringaDiRicerca); 
    void setOffertaMax(Connection conn, int idAsta, int idOfferta);
    Map<Double,Double> getPrezzoOffertaMaxANDRialzoMinimo(Connection conn, int idAsta);
    Asta getOpenAstaById(Connection conn, int idAsta);
    Asta getClosedAstaById(Connection conn, int idAsta);
    boolean astaCanBeClosed(Connection conn, int idAsta);
    void setAstaAsClosed(Connection conn, int idAsta, String username);
    Map<Asta, ArrayList<String>> getInfoFromAClosedAsta(Connection conn, int idAsta);
    boolean checkCreatorOfAsta(Connection conn, String username, int idAsta);

}