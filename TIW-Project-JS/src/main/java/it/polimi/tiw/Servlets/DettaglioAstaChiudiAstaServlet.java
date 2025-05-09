package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DettaglioAstaChiudiAstaServlet extends HttpServlet {
	//Consigliato da Eclipse
	private static final long serialVersionUID = 1L; 
	private AsteDAOImpl asteDAO;

    public void init() throws ServletException {
        asteDAO = new AsteDAOImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer idAsta;
        
        //se la sessione non esiste o l'username non è in sessione rimanda tutti alla pagina di login
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        //controllo se c'è l'idAsta in sessione, se no errore (controllo che viene fatto solo qui, per questo non è unito al controllo sopra)
        if(session.getAttribute("idAsta") == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sessione mancante di dati : non c'è l'idAsta in Sessione.");
            return;
        }

        //faccio il casting senza fare "parse" dato che i dati provengono da una sessione (si presume "sicuri")
        idAsta = (Integer) session.getAttribute("idAsta");
        String username = (String) session.getAttribute("username");


        try (Connection conn = ConnectionManager.getConnection()) {
            
        	// Verifica che l'utente sia il creatore dell'asta
            boolean isCreator = asteDAO.checkCreatorOfAsta(conn, username, idAsta);
            if (!isCreator) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato a chiudere questa asta.");
                return;
            }

            // Verifica che l'asta possa essere chiusa
            boolean canBeClosed = asteDAO.astaCanBeClosed(conn, idAsta);
            if (canBeClosed) {
            	//chiudo l'asta
                asteDAO.setAstaAsClosed(conn, idAsta, username);
            }else{
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "L'asta non può essere chiusa.");
                return;
            }

        } catch (SQLException e) {
            throw new ServletException("Errore durante la chiusura dell'asta. -> query error", e);
        }


        // Dopo aver fatto tutto reindirizza alla pagina servlet di costruzione della pagina di dettaglio asta
        response.sendRedirect(request.getContextPath() + "/dettaglioAstaPage?idAsta="+idAsta);
    }
}
