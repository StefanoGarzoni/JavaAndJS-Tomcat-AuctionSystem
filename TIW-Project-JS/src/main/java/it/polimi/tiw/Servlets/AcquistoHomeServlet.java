package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;
import it.polimi.tiw.dao.Beans.Offerta;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

@MultipartConfig
public class AcquistoHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Gson gson = new Gson();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		HttpSession session = request.getSession(false);
		
		if(session == null) {	// verify if the client is authenticated
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		String username = (String) request.getSession().getAttribute("username");
		
		String asteVisionateJsonCookie = null;
		Cookie[] cookies = request.getCookies();
		
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals("asteVisionate")) {
				asteVisionateJsonCookie = cookie.getValue();
			}
		}
		
		JsonObject finalObject = new JsonObject();
		try(Connection conn = ConnectionManager.getConnection()){
			
			// se è presente il cookie, invio le aste visionate
			if(asteVisionateJsonCookie != null) {
				// estraggo gli id delle aste visionate dal json
				JsonArray idAsteVisionate = JsonParser.parseString(asteVisionateJsonCookie).getAsJsonArray();
				ArrayList<Integer> idAste = new ArrayList<>();
				for (JsonElement idAsta : idAsteVisionate) {
					try {
						idAste.add(idAsta.getAsInt());
					}
					catch(NumberFormatException e) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Format of parameters not accepted");
						return;
					}
				}
			
				// carico le aste visionate
				ArrayList<Asta> asteVisionate = new AsteDAOImpl().getAsteById(conn, idAste);
				
				JsonArray jsonArrayAsteVisionate = gson.toJsonTree(asteVisionate).getAsJsonArray();
				finalObject.add("asteVisionate", jsonArrayAsteVisionate);
			}	
			
			// carico le offerte aggiudicate dall'utente (in ogni caso)
			ArrayList<Offerta> offerteAggiudicate = new OfferteDAOImpl().getAsteAggiudicateByUsername(conn, username);
			
			JsonArray offerteAggiudicateJsonArray = new JsonArray();
			for(Offerta offerta : offerteAggiudicate) {
				JsonObject offertaAggiudicataCustom = new JsonObject();
				offertaAggiudicataCustom.addProperty("idAsta", offerta.getIdAsta());
				offertaAggiudicataCustom.addProperty("prezzoFinale", offerta.getPrezzo());
				
				ArrayList<Articolo> articoli = new ArticoliDAOImpl().getArticoliByIdAsta(conn, offerta.getIdAsta());
				JsonArray jsonArrayArticoli = gson.toJsonTree(articoli).getAsJsonArray();
				offertaAggiudicataCustom.add("articoli", jsonArrayArticoli);
				
				offerteAggiudicateJsonArray.add(offertaAggiudicataCustom);
			}
			finalObject.add("asteCustomAggiudicate", offerteAggiudicateJsonArray);
			
			String jsonResponse = gson.toJson(finalObject);
			
			// impostazione content-type e charset della risposta
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			// scrivo il json nella response
			PrintWriter out = response.getWriter();
			out.print(jsonResponse);
			out.flush();
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
		}
	}
	
	// mostra la pagina con in aggiunta la tabella delle aste con la parola chiave
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		HttpSession session = request.getSession(false);
		
		if(session == null) {	// verify if the client is authenticated
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		String keyword = request.getParameter("parolaChiave");
		
		if(keyword == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		String username = (String) session.getAttribute("username");
		
		try(Connection conn = ConnectionManager.getConnection()){
			ArrayList<Asta> openAste = new AsteDAOImpl().getAsteByStringInArticoli(conn, keyword, username);
			
			String jsonResponse = gson.toJson(openAste);			
			
		    // impostazione content-type e charset della risposta
		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    
		    // scrivo il json nella response
		    PrintWriter out = response.getWriter();
		    out.print(jsonResponse);
		    out.flush();
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
		}
	}
}
