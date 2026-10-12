package in.onewish.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8"); res.setCharacterEncoding("UTF-8");
        HttpServletResponse h=(HttpServletResponse)res; h.setHeader("X-Content-Type-Options","nosniff"); h.setHeader("X-Frame-Options","DENY"); h.setHeader("Referrer-Policy","strict-origin-when-cross-origin");
        chain.doFilter(req,res);
    }
}
