//Interfaccia DAO Tabella Offerte

package it.polimi.tiw.dao;


import java.util.*;
import it.polimi.tiw.dao.Beans.Offerta;
import java.sql.Connection;

public interface OfferteDAO{
    ArrayList<Offerta> getOfferteMaxByUsername(Connection conn, String username);
    ArrayList<Offerta> getOfferteByIdAsta(Connection conn, int idAsta);
    int insertNewOfferta(Connection conn, int idAsta, String username, double prezzo);
    ArrayList<Offerta> getOfferteInOpenAsta(Connection conn, int idAsta); 

}