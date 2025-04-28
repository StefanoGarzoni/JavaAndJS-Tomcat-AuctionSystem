//Implementazione DAO Tabella Aste

package it.polimi.tiw.DAO;
import it.polimi.tiw.DAO.Beans.Asta;
import java.sql.Time;
import java.util.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AsteDAOImpl implements AsteDAO{

    @Override
    public void insertNewAsta(Connection conn, String usernameCreatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza) {
        String query = "INSERT INTO Aste (creatore, prezzo_iniziale, rialzo_minimo, data_scadenza, ora_scadenza) VALUES (?, ?, ?, ?, ?);";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, usernameCreatore);
            ps.setDouble(2, prezzoIniziale);
            ps.setDouble(3, rialzoMinimo);
            ps.setDate(4, dataScadenza);
            ps.setTime(5, oraScadenza);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore in insertNewAsta", e);
        }
    }

    @Override
    public ArrayList<Asta> getAllClosedAsteInfoByCreator(Connection conn, String usernameCreatore) {
        String query = "SELECT * FROM Aste WHERE creatore = ? AND chiusa = True;";
        ArrayList<Asta> aste = new ArrayList<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, usernameCreatore);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                Asta asta = new Asta(
                    result.getInt("id_asta"),
                    result.getString("creatore"),
                    result.getDouble("prezzo_iniziale"),
                    result.getDouble("rialzo_minimo"),
                    result.getDate("data_scadenza"),
                    result.getTime("ora_scadenza"),
                    result.getInt("offerta_max"),
                    result.getBoolean("chiusa")
                );
                aste.add(asta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getAllClosedAsteInfoByCreator", e);
        }
        return aste;
    }

    @Override
    public ArrayList<Asta> getAllOpenAsteInfoByCreator(Connection conn, String usernameCreatore) {
        String query = "SELECT * FROM Aste WHERE creatore = ? AND chiusa = False;";
        ArrayList<Asta> aste = new ArrayList<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, usernameCreatore);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                Asta asta = new Asta(
                    result.getInt("id_asta"),
                    result.getString("creatore"),
                    result.getDouble("prezzo_iniziale"),
                    result.getDouble("rialzo_minimo"),
                    result.getDate("data_scadenza"),
                    result.getTime("ora_scadenza"),
                    result.getInt("offerta_max"),
                    result.getBoolean("chiusa")
                );
                aste.add(asta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getAllOpenAsteInfoByCreator", e);
        }
        return aste;
    }

    @Override
    public ArrayList<Asta> getAsteByStringInArticoli(Connection conn, String stringaDiRicerca) {
        String query = "SELECT Aste.* FROM Aste JOIN Articoli ON Aste.id_asta = Articoli.id_asta WHERE (data_scadenza < CURDATE() OR (data_scadenza = CURDATE() AND ora_scadenza < CURTIME())) AND (descrizione LIKE ? OR nome LIKE ?) ORDER BY data_scadenza DESC, ora_scadenza DESC;";
        ArrayList<Asta> aste = new ArrayList<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, "%" + stringaDiRicerca + "%");
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                Asta asta = new Asta(
                    result.getInt("id_asta"),
                    result.getString("creatore"),
                    result.getDouble("prezzo_iniziale"),
                    result.getDouble("rialzo_minimo"),
                    result.getDate("data_scadenza"),
                    result.getTime("ora_scadenza"),
                    result.getInt("offerta_max"),
                    result.getBoolean("chiusa")
                );
                aste.add(asta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getAsteByStringInArticoli", e);
        }
        return aste;
    }

    @Override
    public void setOffertaMax(Connection conn, int idAsta, int idOfferta) {
        String query = "UPDATE Aste SET offerta_max = ? WHERE id_asta = ?;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idOfferta);
            ps.setInt(2, idAsta);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore in setOffertaMax", e);
        }
    }

    @Override
    public Map<Double,Double> getPrezzoOffertaMaxANDRialzoMinimo(Connection conn, int idAsta) {
        String query = "SELECT prezzo, rialzo_minimo FROM Aste JOIN Offerte ON offerta_max = id_offerta WHERE id_asta = ? ;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                Map<Double, Double> results = new HashMap<>();
                results.put(result.getDouble("prezzo"), result.getDouble("rialzo_minimo"));
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getPrezzoOffertaMax", e);
        }
        return null;
    }

    @Override
    public Asta getOpenAstaById(Connection conn, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = False;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                return new Asta(
                    result.getInt("id_asta"),
                    result.getString("creatore"),
                    result.getDouble("prezzo_iniziale"),
                    result.getDouble("rialzo_minimo"),
                    result.getDate("data_scadenza"),
                    result.getTime("ora_scadenza"),
                    result.getInt("offerta_max"),
                    result.getBoolean("chiusa")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getOpenAstaById", e);
        }
        return null;
    }

    @Override
    public Asta getClosedAstaById(Connection conn, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = True;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                return new Asta(
                    result.getInt("id_asta"),
                    result.getString("creatore"),
                    result.getDouble("prezzo_iniziale"),
                    result.getDouble("rialzo_minimo"),
                    result.getDate("data_scadenza"),
                    result.getTime("ora_scadenza"),
                    result.getInt("offerta_max"),
                    result.getBoolean("chiusa")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getClosedAstaById", e);
        }
        return null;
    }

    @Override
    public boolean astaCanBeClosed(Connection conn, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = False AND (data_scadenza < CURDATE() OR (data_scadenza = CURDATE() AND ora_scadenza < CURTIME()));";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            if( result.next()) {
                return true;
            }else {
                return false;
            }
         
        } catch (SQLException e) {
            throw new RuntimeException("Errore in astaCanBeClosed", e);
        }
        //return false;
    }

    @Override
    public void setAstaAsClosed(Connection conn, int idAsta, String username) {
        String query = "UPDATE Aste SET chiusa = True WHERE id_asta = ? AND creatore = ?;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore in setAstaAsClosed", e);
        }
    }

    @Override
    public Map<Asta, ArrayList<String>> getInfoFromAClosedAsta(Connection conn, int idAsta) {
        String query = "SELECT Aste.*, Utenti.nome as nomeAggiudicatario, prezzo, indirizzo FROM Aste JOIN Offerte ON offerta_max = id_offerta JOIN Utenti ON utente = username WHERE Aste.id_asta = ? AND chiusa = True;";
        Map<Asta, ArrayList<String>> info = new HashMap<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                Asta asta = new Asta(
                    result.getInt("id_asta"),
                    result.getString("creatore"),
                    result.getDouble("prezzo_iniziale"),
                    result.getDouble("rialzo_minimo"),
                    result.getDate("data_scadenza"),
                    result.getTime("ora_scadenza"),
                    result.getInt("offerta_max"),
                    result.getBoolean("chiusa")
                );
                ArrayList<String> altreInfo = new ArrayList<>();
                altreInfo.add(result.getString("nomeAggiudicatario"));
                altreInfo.add(result.getString("prezzo"));
                altreInfo.add(result.getString("indirizzo"));
                info.put(asta, altreInfo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getInfoFromAClosedAsta", e);
        }
        return info;
    }

    @Override
    public boolean checkCreatorOfAsta(Connection conn, String username, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND creatore = ?;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in checkCreatorOfAsta", e);
        }
        return false;
    }

}
