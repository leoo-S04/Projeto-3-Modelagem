package br.pucpr.ib2b.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cada perfil só abre as suas páginas: /estudante, /atendente, /admin. Sem login, volta para /login.
 * Os pontos de entrada dos bancos (/api/bancos/...) ficam livres: quem chama é o banco, não uma pessoa.
 */
@Configuration
public class AcessoPorPerfil implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(exigir(Usuario.Perfil.ESTUDANTE)).addPathPatterns("/estudante", "/estudante/**", "/simulador/**");
        registro.addInterceptor(exigir(Usuario.Perfil.ATENDENTE)).addPathPatterns("/atendente", "/atendente/**");
        registro.addInterceptor(exigir(Usuario.Perfil.ADMINISTRADOR)).addPathPatterns("/admin", "/admin/**");
    }

    private static HandlerInterceptor exigir(Usuario.Perfil perfil) {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
                Object u = req.getSession().getAttribute(Usuario.CHAVE_NA_SESSAO);
                if (u instanceof Usuario usuario && usuario.perfil() == perfil) return true;
                resp.sendRedirect(req.getContextPath() + (u instanceof Usuario outro ? outro.perfil().inicio() : "/login"));
                return false;
            }
        };
    }
}
