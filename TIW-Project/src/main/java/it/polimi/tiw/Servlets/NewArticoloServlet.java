package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024,      // 1MB in RAM
	    maxFileSize = 1024 * 1024 * 10,       // 10MB per file
	    maxRequestSize = 1024 * 1024 * 50     // 50MB in totale
	)
@WebServlet("/newArticoloServlet")
public class NewArticoloServlet extends HttpServlet {
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
		String redirectionPath = "/TIW-Project/vendo";
		
		String articleName;
		String articleDescription;
		String articlePriceString;
		int articlePrice;
		String imageName;
		
		if(request.getSession(false) == null) {	// if a session already exists (the client logged in)
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		// input sanitise
		try {
			articleName = request.getParameter("nome");
			articleDescription = request.getParameter("descrizione");
			articlePriceString = request.getParameter("prezzo");
			imageName = saveImage(request);
			
			if(articleName == null || articleDescription == null || imageName == null || articlePriceString == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
				return;
			}
			
			articlePrice = Integer.parseInt(articlePriceString);
			
			if(articlePrice < 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Negative price");
				return;
			}
		}
		catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The selected price is not a number");
			return;
		}
		
		// eseguire la query
		try {
			String username = (String) request.getSession().getAttribute("username");
			
			Connection conn = ConnectionManager.getConnection(); 
			new ArticoliDAOImpl().insertNewArticolo(conn, username, articleName, articleDescription, imageName, articlePrice);
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
		}
		
		response.sendRedirect(redirectionPath);
	}	
	
	private String saveImage(HttpServletRequest request) throws ServletException, IOException {
		Part filePart = request.getPart("immagine");	// FIXME: salvataggio immagine
		if(filePart == null) {
			return null;
		}
		
		String fileName = getFileName(filePart);
		
		String uploadPath = getServletContext().getRealPath("") + File.separator + "WEB-INF" + File.separator + "articlesImages";	// file separator serve poichè il separatore dipende dal sistema operativo

        // salva il file
        filePart.write(uploadPath + File.separator + fileName);
	
        return fileName;
	}
	
	private String getFileName(Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {		// recupera l'intestazione e la divide in parti
            if (content.trim().startsWith("filename")) {	// controlla se quella parte di intestazione contiene il filename
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
	}
}
