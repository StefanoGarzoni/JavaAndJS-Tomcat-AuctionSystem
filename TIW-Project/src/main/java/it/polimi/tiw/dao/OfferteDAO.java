//Interfaccia DAO Tabella Offerte

package it.polimi.tiw.dao;


import java.util.*;
import it.polimi.tiw.dao.Beans.Offerta;
import java.sql.Connection;
import java.sql.SQLException;

public interface OfferteDAO{
    ArrayList<Offerta> getOfferteMaxByUsername(Connection conn, String username) throws SQLException;
    ArrayList<Offerta> getOfferteByIdAsta(Connection conn, int idAsta) throws SQLException;
    int insertNewOfferta(Connection conn, int idAsta, String username, double prezzo) throws SQLException;
    ArrayList<Offerta> getOfferteInOpenAsta(Connection conn, int idAsta) throws SQLException; 
}