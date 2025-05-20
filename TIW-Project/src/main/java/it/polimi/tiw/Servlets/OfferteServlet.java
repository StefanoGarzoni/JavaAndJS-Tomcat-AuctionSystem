package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Offerta;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public class OfferteServlet extends HttpServlet {
	
	//consigliato da Eclipse
    private static final long serialVersionUID = 1L;

    private OfferteDAOImpl offerteDAO;
    private ArticoliDAOImpl articoliDAO;
    private AsteDAOImpl asteDAO;
    private TemplateEngine templateEngine;
    
    JakartaServletWebApplication webApplication;
    WebApplicationTemplateResolver resolver;
    ServletContext servletContext;
    
    public void init() throws ServletException {

    	servletContext = getServletContext();
        offerteDAO = new OfferteDAOImpl();
        articoliDAO = new ArticoliDAOImpl();
        asteDAO = new AsteDAOImpl();

        webApplication = JakartaServletWebApplication.buildApplication(servletContext);
        resolver = new WebApplicationTemplateResolver(webApplication);

        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer idAsta;
        
        //controllo l'esistenza della sessione e se l'username è in sessione, altrimenti rimando a login
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        //prendo il parametro idAsta dal get
        String idAstaParam = request.getParameter("idAsta");
        if (idAstaParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ID asta non specificato");
            return;
        }

        //provo a fare il parsing in intero del paramtro che ricevo dal get (idAsta)
        try{
            idAsta = Integer.parseInt(idAstaParam);
            
            //se riesce lo inserisco anchein sessione
            session.setAttribute("idAsta", idAsta);
            
        }catch(Exception e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ID asta non valido");
            return;
        }
        
        //preparo le strutture dati che riceveranno i dati dal db dopo le query
        ArrayList<Articolo> articoli;
        ArrayList<Offerta>  offerte;

        try (Connection conn = ConnectionManager.getConnection()) {
        	
        	//recupero articoli e offerte dato l'idAsta
            articoli = articoliDAO.getArticoliByIdAsta(conn, idAsta);
            offerte  = offerteDAO.getOfferteByIdAsta(conn, idAsta);
            
        } catch (SQLException e) {
        	e.printStackTrace(System.out);
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Errore nel recuperare articoli e/o offerte dal database");
        	return;
        }
        
        //provo a scaricare dal db il rialzo minimo
        Double rialzo = 0.0;
        try (Connection conn = ConnectionManager.getConnection()) {
            rialzo = asteDAO.getRialzoMinimo(conn, idAsta);
           
        } catch (SQLException e) {
        	e.printStackTrace(System.out);
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Errore nel recupero del rialzo minimo");
        	return;
        }

        // Preparazione modello Thymeleaf
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("articoli", articoli);
        ctx.setVariable("offerte", offerte);
        ctx.setVariable("rialzo_minimo", rialzo);
        
        if(request.getParameter("PriceTooLow")!=null) {
        	
        	//setto una "variabile" da passare a thymeleaf che mi stampa un "errore" -> prezzo non valido
        	ctx.setVariable("PriceTooLow", request.getParameter("PriceTooLow"));
        }
        
        // Render del template
        response.setContentType("text/html;charset=UTF-8");
        try (Writer writer = response.getWriter()) {
            templateEngine.process("offerta", ctx, writer);
        }
    }
}
