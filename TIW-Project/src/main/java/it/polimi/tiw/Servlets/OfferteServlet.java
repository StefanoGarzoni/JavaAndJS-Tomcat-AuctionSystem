package it.polimi.tiw.Servlets;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Offerta;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

// @WebServlet("/offertePage") commentato perchè causa conflitti con web.xml
public class OfferteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private OfferteDAOImpl offerteDAO;
    private ArticoliDAOImpl articoliDAO;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {

        ServletContext servletContext = getServletContext();
        offerteDAO   = new OfferteDAOImpl();
        articoliDAO  = new ArticoliDAOImpl();

        JakartaServletWebApplication webApp=JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver resolver=new WebApplicationTemplateResolver(webApp);

        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //FARE if che controlla se c'è la sessione altrimenti rimando al login con un errore (DA FARE IN TUTTE LE SERVLET)

        HttpSession session = request.getSession(false);

        //idAsta proviene dal get non dalla sessione
        Integer idAsta= (session != null) ? (Integer) session.getAttribute("idAsta") : null;

        if (idAsta == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ID asta non specificato");
            return;
        }

        ArrayList<Articolo> articoli;
        ArrayList<Offerta>  offerte;

        try (Connection conn = ConnectionManager.getConnection()) {
            articoli = articoliDAO.getArticoliByIdAsta(conn, idAsta);
            offerte  = offerteDAO.getOfferteByIdAsta(conn, idAsta);
        } catch (SQLException e) {
            throw new ServletException("Errore durante il recupero dei dati", e);
        }

        // Preparazione modello Thymeleaf
        Context ctx = new Context(request.getLocale());
        ctx.setVariable("articoli", articoli);
        ctx.setVariable("offerte", offerte);

        // Render del template
        response.setContentType("text/html;charset=UTF-8");
        try (Writer writer = response.getWriter()) {
            templateEngine.process("offerte", ctx, writer);
        }
    }
}
