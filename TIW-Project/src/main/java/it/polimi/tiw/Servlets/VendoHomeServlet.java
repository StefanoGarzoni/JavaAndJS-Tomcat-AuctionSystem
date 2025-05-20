package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAO;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAO;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class VendoHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private TemplateEngine templateEngine;
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
		
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String vendoPath = "/vendo.html";
		
		HttpSession session = request.getSession(false);
		
		if(session == null) {	// se un utente non è loggato, lo reindirizza al login
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		// se presente, azzera idAsta precedentemente impostato per mostrare l'asta corretta in DettaglioAsta
		if(session.getAttribute("idAsta") != null) {
			session.removeAttribute("idAsta");
		}
		
		AsteDAO asteDAO = new AsteDAOImpl();
		ArticoliDAO articoliDAO = new ArticoliDAOImpl();
		
		String username = (String) session.getAttribute("username");
		LocalDateTime lastLoginTimestamp = (LocalDateTime) session.getAttribute("lastLoginTimestamp");
		
		try(Connection conn = ConnectionManager.getConnection()){
			ArrayList<Asta> openAste = asteDAO.getAllOpenAsteInfoByCreator(conn, username);
			setTempoRimanenteInAste(openAste, lastLoginTimestamp);
			
			ArrayList<Asta> closedAste = asteDAO.getAllClosedAsteInfoByCreator(conn, username);
			
			ArrayList<Articolo> availableArticoli = articoliDAO.getMyArticoli(conn, username);
			
			JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
			WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
			ctx.setVariable("asteAperte", openAste);
			ctx.setVariable("asteChiuse", closedAste);
			ctx.setVariable("availableArticoli", availableArticoli);
			
			// imposta una variabile nel contesto per segnalare che la data inserita nella precedente creazione di un'asta 
			// è nel passato e perciò non consentita
			if(request.getParameter("passedExpirationData") != null) ctx.setVariable("passedExpirationData", true);
			
			// imposta una variabile nel contesto per segnalare che c'è stato un problema nell'inerimento dell'asta
			if(request.getParameter("errorWhileCreatingAsta") != null) ctx.setVariable("errorWhileCreatingAsta", true);
			
			// imposta una variabile nel contesto per segnalare che l'asta che si è provato ad inserire non aveva articoli selezionati
			if(request.getParameter("emptyArticlesList") != null) ctx.setVariable("emptyArticlesList", true);
			
			templateEngine.process(vendoPath, ctx, response.getWriter());
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Errore nel recuperare articoli e/o offerte dal database");
			return;
		}
	}
	
	private void setTempoRimanenteInAste(ArrayList<Asta> aste, LocalDateTime lastLoginTimestamp) {
		for(Asta asta : aste) {
			
			// conversion from java.sql.Date and java.sql.Time to LocalDateTime
			LocalDateTime scadenzaAsta = LocalDateTime.of(
					asta.getDataScadenza().toLocalDate(), 
					asta.getOraScadenza().toLocalTime()
			);
			
			long giorniRimanentiAsta = ChronoUnit.DAYS.between(lastLoginTimestamp, scadenzaAsta);	// integer days distance
			LocalDateTime dopoGiorni = lastLoginTimestamp.plusDays(giorniRimanentiAsta);
			long oreRimanentiAsta = Duration.between(dopoGiorni, scadenzaAsta).toHours();	// integer hours distance
			
			asta.setGiorniRimanenti((int)giorniRimanentiAsta);
			asta.setOreRimanenti((int)oreRimanentiAsta);
		}
	}
}
