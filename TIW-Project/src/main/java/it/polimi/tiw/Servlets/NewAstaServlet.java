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
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/**
 * 
 */
@WebServlet("/NewAstaServlet")
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
		if(selectedArticlesStrings == null || rialzoMinimoString == null || dataScadenzaString == null || oraScadenzaString == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
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
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "La data di scadenza scelta è nel passato");
				return;
			}
		}
		catch (DateTimeParseException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Data o ora di scadenza non sono date");
			return;
		}
		
		// new asta queries
		try {
			ArticoliDAO articoliDAO = new ArticoliDAOImpl();
			
			Connection conn = ConnectionManager.getConnection();
			String username = (String) request.getSession().getAttribute("username");
			
			if(!selectedArticlesIds.isEmpty()) {
				
				// controllo che l'utente possegga tutti gli articoli che vengono messi all'asta
				if(!articoliDAO.areAllArticlesOfUser(conn, username, selectedArticlesIds)) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can select only your own articles");
					return;
				}
			
				int startingPrice = articoliDAO.getSumOfPrice(conn, selectedArticlesIds);
				
				AsteDAO asteDAO = new AsteDAOImpl();
				int newAstaId = asteDAO.insertNewAsta(
					conn, 
					username, 
					startingPrice, 
					rialzoMinimo, 
					java.sql.Date.valueOf(data),
					java.sql.Time.valueOf(ora)
				);
				
				// idAsta update in all selected articles
				articoliDAO.updateIdAstaInArticles(conn, selectedArticlesIds, newAstaId);
			}
			// else non fa niente?
			
			response.sendRedirect(redirectPath);
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
		}
	}	
}
