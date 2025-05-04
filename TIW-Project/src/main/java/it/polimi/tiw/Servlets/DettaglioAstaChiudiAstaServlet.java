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

// @WebServlet("/chiudiAsta") commentato perchè causa conflitti con web.xml
public class DettaglioAstaChiudiAstaServlet extends HttpServlet {

	private static final long serialVersionUID = 1L; //Consigliato da Eclipse non so il perchè
	private AsteDAOImpl asteDAO;

    public void init() throws ServletException {
        asteDAO = new AsteDAOImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        String username = (String) session.getAttribute("username");

        String chiudiParam = request.getParameter("chiudi");

        if (idAsta == null || username == null || chiudiParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti.");
            return;
        }

        boolean chiudi = Boolean.parseBoolean(chiudiParam);

        if (chiudi) {
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
                    asteDAO.setAstaAsClosed(conn, idAsta, username);
                }else{
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "L'asta non può essere chiusa.");
                    return;
                }

            } catch (SQLException e) {
                throw new ServletException("Errore durante la chiusura dell'asta.", e);
            }
        }else{
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri errati.");
            return;
        }

        // Dopo tutto reindirizza alla pagina servlet di costruzione pagina di dettaglio asta
        response.sendRedirect(request.getContextPath() + "/dettaglioAstaPage");
    }
}
