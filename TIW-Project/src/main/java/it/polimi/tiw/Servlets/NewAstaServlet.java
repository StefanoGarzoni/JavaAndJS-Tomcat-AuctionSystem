package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAO;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAO;
import it.polimi.tiw.dao.AsteDAOImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class NewAstaServlet extends HttpServlet {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession(false) == null) {	// if a session already exists (the client logged in)
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		String redirectPath = "/TIW-Project/vendo";
		
		// parameters sanitise
		String[] selectedArticlesStrings = request.getParameterValues("codiceArticolo");
		String rialzoMinimoString = request.getParameter("rialzoMinimo");
		String dataScadenzaString = request.getParameter("dataScadenza");
		String oraScadenzaString = request.getParameter("oraScadenza");
		
		// controllo presenza di tutti i parametri
		if( /* selectedArticlesStrings == null || */ rialzoMinimoString == null || dataScadenzaString == null || oraScadenzaString == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		if(selectedArticlesStrings == null) {
			response.sendRedirect(redirectPath+"?emptyArticlesList=true");
			// response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non è possibile creare un'asta senza articoli");
			return;
		}
		
		// controllo che tutti gli id degli articoli scelti siano interi
		ArrayList<Integer> selectedArticlesIds = new ArrayList<>();	
		try {
			for(String articleIdString: selectedArticlesStrings) {
				int parsedId = Integer.parseInt(articleIdString);
				selectedArticlesIds.add(parsedId);
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Some articles identifiers are not number");
			return;
		}
		
		// controllo validità rialzo minimo
		float rialzoMinimo;
		try {
			rialzoMinimo = Float.parseFloat(rialzoMinimoString);
			if(rialzoMinimo <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Negative or zero rialzo minimo");
				return;
			}
		}
		catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Rialzo minimo is not a number");
			return;
		}
		
		// controllo che la data e ora di scadenza siano nel futuro
		LocalDate data;
		LocalTime ora;
		try {
			data = LocalDate.parse(dataScadenzaString);
			ora = LocalTime.parse(oraScadenzaString);
			
			if(
					data.isBefore(LocalDate.now()) || 
					(data.isEqual(LocalDate.now()) && ora.isBefore(LocalTime.now())))
			{
				response.sendRedirect(redirectPath+"?passedExpirationData=true");
				// response.sendError(HttpServletResponse.SC_BAD_REQUEST, "La data di scadenza scelta è nel passato");
				return;
			}
		}
		catch (DateTimeParseException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Data o ora di scadenza non sono date");
			return;
		}
		
		// creazione nuova asta
		String username = (String) request.getSession().getAttribute("username");
		AsteDAO asteDAO = new AsteDAOImpl();
		ArticoliDAO articoliDAO = new ArticoliDAOImpl();
		try {
			Connection conn = ConnectionManager.getConnection();
			
			// controllo che l'utente possegga tutti gli articoli che vengono messi all'asta
			if(!articoliDAO.areAllArticlesOfUser(conn, username, selectedArticlesIds)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can select only your own articles");
				return;
			}
			
			// controllo che gli articoli che si vogliono inserire non siano già in un'altra asta
			if(!articoliDAO.areAllArticlesFree(conn, selectedArticlesIds)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can select only articles that are not in an asta yet");
				return;
			}
		
			conn.setAutoCommit(false);
			try {
				int startingPrice = articoliDAO.getSumOfPrice(conn, selectedArticlesIds);
				
				int newAstaId = asteDAO.insertNewAsta(
						conn, 
						username, 
						startingPrice, 
						rialzoMinimo, 
						java.sql.Date.valueOf(data),
						java.sql.Time.valueOf(ora)
						);
				
				// aggiornamento degli id_asta negli articoli in essa presenti
				articoliDAO.updateIdAstaInArticles(conn, selectedArticlesIds, newAstaId);
				
				// eseguo le modifiche sul DB se tutti i campi sono stati aggiornati correttamente senza errori
				conn.commit();
				response.sendRedirect(redirectPath);
			}
			catch (SQLException e) {
				conn.rollback();
				throw new SQLException("Errore durante l'inserimento in DB dell'asta");
			}
			finally {
				conn.setAutoCommit(true);
			}
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);	// stampo su CLI l'errore SQL
			response.sendRedirect(redirectPath+"?errorWhileCreatingAsta=true");
		}
	}
}
