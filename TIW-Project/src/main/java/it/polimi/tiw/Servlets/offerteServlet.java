package it.polimi.tiw.Servlets;


import it.polimi.tiw.DAO.Beans.Articolo;
import it.polimi.tiw.DAO.Beans.Offerta;
import ConnectionManager; 

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import DAO.ArticoliDAOImpl;
import DAO.OfferteDAOImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet("/offerte/page")
public class offerteServlet extends HttpServlet {

    private OfferteDAOImpl offerteDAO;
    private ArticoliDAOImpl articoliDAO;

    @Override
    public void init() throws ServletException {
        offerteDAO = new OfferteDAOImpl();
        articoliDAO = new ArticoliDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer idAsta = (Integer) session.getAttribute("idAsta");

        if (idAsta == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID asta non specificato");
            return;
        }

        ArrayList<Articolo> articoli = null;
        ArrayList<Offerta> offerte = null;

        try (Connection conn = ConnectionManager.getConnection()) {
           
            articoli = articoliDAO.getArticoliByIdAsta(conn, idAsta);
            offerte = offerteDAO.getOfferteByIdAsta(conn, idAsta);
        } catch (SQLException e) {
            throw new ServletException("Errore durante il recupero dei dati", e);
        }

        request.setAttribute("articoli", articoli);
        request.setAttribute("offerte", offerte);

        // Inoltro alla pagina Thymeleaf
        RequestDispatcher dispatcher = request.getRequestDispatcher("offerte.html");
        dispatcher.forward(request, response);
    }
}
