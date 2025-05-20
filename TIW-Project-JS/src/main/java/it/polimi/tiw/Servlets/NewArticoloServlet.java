package it.polimi.tiw.Servlets;
import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import com.google.gson.Gson;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

//Le immagini sono salvate in: eclipse-workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\TIW-Project\WEB-INF\articlesImages\
// da pietro le immagini solo salvate in: C:\Users\pietr\OneDrive - Politecnico di Milano\PoliMi\3anno\2semestre\TIW - progetto\Progetto-TIPIW\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\TIW-Project\WEB-INF\articlesImages

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 100,      // 100MB in RAM
    maxFileSize = 1024 * 1024 * 100,       // 100MB per file
    maxRequestSize = 1024 * 1024 * 500     // 500MB in totale
)
public class NewArticoloServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Gson gson = new Gson();
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String articleName;
		String articleDescription;
		String articlePriceString;
		int articlePrice;
		String imageName;
		
		if(request.getSession(false) == null) {	// if a session already exists (the client logged in)
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		// estrazione e sanificazione input
		articleName = request.getParameter("nome");
		articleDescription = request.getParameter("descrizione");
		articlePriceString = request.getParameter("prezzo");
		
		imageName = saveImage(request);
			
		if(articleName == null || articleDescription == null || imageName == null || articlePriceString == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Parametri mancanti o non validi\"}");
            return;
		}
		
		try {
			articlePrice = Integer.parseInt(articlePriceString);
		}
		catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Prezzo non valido\"}");
			return;
		}
		
		if(articlePrice < 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Prezzo non valido\"}");
			return;
		}
		
		// eseguire la query
		try {
			String username = (String) request.getSession().getAttribute("username");
			
			Connection conn = ConnectionManager.getConnection(); 
			Articolo newArticolo = new ArticoliDAOImpl().insertNewArticolo(conn, username, articleName, articleDescription, imageName, articlePrice);
			
			String finalJson = gson.toJson(newArticolo);
			
			// imposto il cookie con la nuova azione
			Cookie lastAction = new Cookie("lastAction", "addedArticolo");
			lastAction.setMaxAge(30*60*60*24);	// scadenza a 30gg
            response.addCookie(lastAction);
		    
			// imposto content-type e charset della risposta
		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");

		    // scrittura JSON nella response
		    PrintWriter out = response.getWriter();
		    out.print(finalJson);
		    out.flush();
		}
		catch (SQLException e) {
		    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\""+e+"\"}");
            e.printStackTrace(System.out);
		}
	}	
	
	private String saveImage(HttpServletRequest request) throws ServletException, IOException {
		Part filePart = request.getPart("immagine");
		if(filePart == null) {
			return null;
		}
		
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();	// estrazione del nome del file dell'immagine
		String extension = "";
		
		int i = fileName.lastIndexOf('.');
		if (i > 0) extension = fileName.substring(i); 	// estrae l'estensione del file
		String uniqueName = UUID.randomUUID().toString() + extension;		// genera un nome casuale per salvare il file (per evitare più file con lo stesso nome)
		
		String uploadPath = getServletContext().getInitParameter("articlesImagesUploadPath");
		
        // scrive l'immagine nel file
        filePart.write(uploadPath + File.separator + uniqueName);
	
        return uniqueName;
	}

}
