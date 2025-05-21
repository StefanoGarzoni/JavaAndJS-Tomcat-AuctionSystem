package it.polimi.tiw.Servlets;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.Beans.Asta;
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

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class DettaglioAstaPageServlet extends HttpServlet {
	//consigliato da Eclipse
    private static final long serialVersionUID = 1L;
    
    //oggetti DAO e templateEngine
    private OfferteDAOImpl offerteDAO;
    private AsteDAOImpl asteDAO;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication webApplication;
    private  WebApplicationTemplateResolver templateResolver;

    public void init() throws ServletException {

        offerteDAO = new OfferteDAOImpl();
        asteDAO    = new AsteDAOImpl();
        
        //set di attributi e costruzione oggetti per la costruzione del template thymeleaf
        webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer idAsta;
        
        //se la sessione non è creata o non c'è l'username
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        //controllo che ci sia il parametro idAsta nel get 
        String idAstaParam = request.getParameter("idAsta");
        if (idAstaParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ID asta non specificato : non c'è nel get");
            return;
        }
        
        //provo il parsing dell'idAsta -> se riesco salvo anche in sessione l'idAsta
        try{
            idAsta = Integer.parseInt(idAstaParam);
            session.setAttribute("idAsta", idAsta);
        }catch(Exception e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ID asta non valido : errore nel parsing");
            return;
        }
        
        //provo a estrarre l'username dalla sessione
        String username = (String)session.getAttribute("username");
        if (username == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"username non valido in sessione.");
            return;
        }

        //costruzione del webContext
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());

        try (Connection conn = ConnectionManager.getConnection()) {
        	
        	//controllo che l'utente che richiede i dettagli sia il creatore dell'asta
            boolean isCreator = asteDAO.checkCreatorOfAsta(conn, username, idAsta);
            if (!isCreator) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"Non sei il creatore dell'asta. Non puoi vederla.");
                return;
            }
            
            //controllo se l'asta è aperta o no
            Asta openAsta = asteDAO.getOpenAstaById(conn, idAsta);
            if (openAsta != null) {
            	
            	//asta aperta
                ctx.setVariable("openAsta", openAsta);
                
                //l'asta può essere chiusa?
                ctx.setVariable("canBeClosed", asteDAO.astaCanBeClosed(conn, idAsta));
                
                //scarico i dettagli delle offerte sull'asta
                ArrayList<Offerta> offerte =offerteDAO.getOfferteInOpenAsta(conn, idAsta);
                
                ctx.setVariable("offerte", offerte);

            } else {
            		
            		//scaricamento delle informazioni necessarie se l'asta è chiusa
                    Map<Asta, ArrayList<String>> closedInfo = asteDAO.getInfoFromAClosedAsta(conn, idAsta);
                    Asta astaChiusa = closedInfo.keySet().iterator().next();
                    ArrayList<String> info = closedInfo.get(astaChiusa);

                    ctx.setVariable("astaChiusa", astaChiusa);
                    ctx.setVariable("nomeAcquirente", info.get(0));
                    ctx.setVariable("prezzo", info.get(1));
                    ctx.setVariable("indirizzo", info.get(2));
            }
        } catch (SQLException e) {
        	e.printStackTrace(System.out);
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Errore durante la ricerca della pagina");
        }

        response.setContentType("text/html;charset=UTF-8");
        
        try (Writer writer = response.getWriter()) {
        	
        	//mando tutto a thymeleaf
            templateEngine.process("dettaglioAsta", ctx, writer);
        }
    }
}
