package in.onewish.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Allows browser calls only from the configured Netlify origin. */
@WebFilter("/api/*")
public class CorsFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest)request; HttpServletResponse res=(HttpServletResponse)response;
        String allowed=System.getenv("CORS_ALLOWED_ORIGIN"), origin=req.getHeader("Origin");
        if(origin!=null && allowed!=null && origin.equals(allowed)) {
            res.setHeader("Access-Control-Allow-Origin",allowed); res.setHeader("Access-Control-Allow-Credentials","true");
            res.setHeader("Access-Control-Allow-Methods","GET, POST, PATCH, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers","Content-Type, X-CSRF-Token"); res.setHeader("Vary","Origin");
        }
        if("OPTIONS".equalsIgnoreCase(req.getMethod())) { res.setStatus(origin!=null && origin.equals(allowed)?204:403); return; }
        chain.doFilter(request,response);
    }
}
