package br.com.nascisoft.apoioterritoriols.login.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import br.com.nascisoft.apoioterritoriols.cadastro.client.LoginService;
import br.com.nascisoft.apoioterritoriols.cadastro.vo.LoginVO;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(LoginServiceImpl.class.getName());
	
	private static final List<String> usuariosValidos;
	
	static {
		usuariosValidos = new ArrayList<String>();
		usuariosValidos.add("tiagonn@gmail.com");
		usuariosValidos.add("monteiro.camila@gmail.com");
		usuariosValidos.add("ls.centralcampinas@gmail.com");
	}

	@Override
	public LoginVO login(String requestURI) {
		
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();		
		LoginVO usuario = new LoginVO();
		
		if (user != null && usuariosValidos.contains(user.getEmail())) {
			logger.info("Tentando realizar o login do usuario " + 
					user.getUserId() + ", e-mail: " + user.getEmail());
			usuario.setEmail(user.getEmail());
			usuario.setIdentificador(user.getUserId());
			usuario.setNickname(user.getNickname());
			usuario.setLogado(true);
			usuario.setLogoutURL(userService.createLogoutURL(requestURI));
		} else {
			logger.info("Usu�rio n�o logado, redirecionando para tela de login");
			usuario.setLogado(false);
			usuario.setLoginURL(userService.createLoginURL(requestURI));
		}
		return usuario;
	}

}
