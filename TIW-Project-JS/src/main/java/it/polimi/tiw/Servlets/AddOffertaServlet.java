package it.polimi.tiw.Servlets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
public class AddOffertaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Gson gson = new Gson();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
//    response.setContentType("application/json;charset=UTF-8");
//    PrintWriter out = response.getWriter();
//    JsonObject jo = new JsonObject();
//    try {
//      NewOffertaDto dto = gson.fromJson(request.getReader(), NewOffertaDto.class);
//      OffertaService.submitOffer(dto);
//      jo.addProperty("success", true);
//      out.print(gson.toJson(jo));
//    } catch (IllegalArgumentException ia) {
//      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//      jo.addProperty("error", ia.getMessage());
//      out.print(gson.toJson(jo));
//    } catch (Exception e) {
//      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//      jo.addProperty("error", "Errore inserimento offerta: " + e.getMessage());
//      out.print(gson.toJson(jo));
//    }
//    out.flush();
  }
}
