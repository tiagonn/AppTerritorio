package br.com.nascisoft.apoioterritoriols.cadastro.server.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.com.nascisoft.apoioterritoriols.login.entities.Mapa;
import br.com.nascisoft.apoioterritoriols.login.entities.Surdo;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.util.DAOBase;

public class CadastroDAO extends DAOBase {
	
//	private static final Logger logger = Logger.getLogger(CadastroDAO.class.getName());
	
	public Long adicionarOuAlterarSurdo(Surdo surdo) {
		Objectify ofy = ObjectifyService.begin();
		ofy.put(surdo);
		return surdo.getId();
	}
	
	public List<Surdo> obterSurdos(String nomeSurdo, String nomeRegiao, Long identificadorMapa, Boolean estaAssociadoMapa) {
		Objectify ofy = ObjectifyService.begin();
		Query<Surdo> query = ofy.query(Surdo.class);
		if (nomeSurdo != null && nomeSurdo.length() > 0) {
			query.filter("nome >=", nomeSurdo.toUpperCase()).filter("nome <", nomeSurdo.toUpperCase() + '\uFFFD');
		}
		if (nomeRegiao != null && nomeRegiao.length() > 0) {
			query.filter("regiao", nomeRegiao);
		}
		if (identificadorMapa != null) {
			Key<Mapa> key = new Key<Mapa>(Mapa.class, identificadorMapa);
			query.filter("mapa", key);
		}
		if (estaAssociadoMapa != null) {
			query.filter("estaAssociadoMapa", estaAssociadoMapa);
		}
		query.filter("mudouSe", Boolean.FALSE);
		query.filter("visitarSomentePorAnciaos", Boolean.FALSE);
		query.order("nome");
		return query.list();
	}
	
	public Mapa obterMapa(Key<Mapa> chave) {
		Objectify ofy = ObjectifyService.begin();
		return ofy.get(chave);
	}
	
	public Map<Key<Mapa>, Mapa> obterMapas(Collection<Key<Mapa>> chave) {
		Objectify ofy = ObjectifyService.begin();
		return ofy.get(chave);
	}
	
	public Surdo obterSurdo(Long id) {
		Objectify ofy = ObjectifyService.begin();
		return ofy.get(Surdo.class, id);
	}
	
	public Long adicionarMapa(String nomeRegiao, String letra) {
		Objectify ofy = ObjectifyService.begin();
		List<Mapa> mapas = this.obterMapasRegiao(nomeRegiao);
		Integer i = 1;
		for (Mapa mapa : mapas) {
			if (i.equals(mapa.getNumero())) {
				i++;
			} else {
				break;
			}
		}
		
		Mapa mapa = new Mapa();
		mapa.setNumero(i);
		mapa.setLetra(letra);
		mapa.setRegiao(nomeRegiao);
		
		ofy.put(mapa);
		
		return mapa.getId();
		
	}
	
	public List<Mapa> obterMapasRegiao(String nomeRegiao) {
		Objectify ofy = ObjectifyService.begin();
		Query<Mapa> query = ofy.query(Mapa.class);
		if (nomeRegiao != null && nomeRegiao.length() > 0) {
			query.filter("regiao", nomeRegiao);
		}
		query.order("numero");
		return query.list();
	}
	
	public List<Surdo> obterSurdosSemMapa(String nomeRegiao) {
		Objectify ofy = ObjectifyService.begin();
		Query<Surdo> query = ofy.query(Surdo.class);
		if (nomeRegiao != null && nomeRegiao.length() > 0) {
			query.filter("regiao", nomeRegiao);
		}
		query.filter("estaAssociadoMapa", Boolean.FALSE);
		query.filter("mudouSe", Boolean.FALSE);
		query.filter("visitarSomentePorAnciaos", Boolean.FALSE);

		query.order("nome");
		return query.list();
	}
	
	public List<Surdo> obterSurdosOutrosMapas(String nomeRegiao, Long identificadorMapa) {
		Objectify ofy = ObjectifyService.begin();
		Query<Surdo> query = ofy.query(Surdo.class);
		if (nomeRegiao != null && nomeRegiao.length() > 0) {
			query.filter("regiao", nomeRegiao);
		}
		if (identificadorMapa != null) {
			Key<Mapa> key = new Key<Mapa>(Mapa.class, identificadorMapa);
			query.filter("mapa !=", key);
		}
		query.filter("estaAssociadoMapa", Boolean.TRUE);
		query.filter("mudouSe", Boolean.FALSE);
		query.filter("visitarSomentePorAnciaos", Boolean.FALSE);
		
		return query.list();
	}
	
	public void adicionarSurdoMapa(List<Long> surdos, Long identificadorMapa) {
		Objectify ofy = ObjectifyService.begin();
		for (Long id : surdos) {
			Surdo surdo = ofy.get(Surdo.class, id);
			Key<Mapa> key = new Key<Mapa>(Mapa.class, identificadorMapa);
			surdo.setMapa(key);
			ofy.put(surdo);
		}
	}
	
	public Long removerSurdoMapa(List<Long> surdos) {
		Long retorno = null;
		Objectify ofy = ObjectifyService.begin();
		for (Long id : surdos) {
			Surdo surdo = ofy.get(Surdo.class, id);
			if (retorno == null) {
				retorno = surdo.getMapa().getId();
			}
			surdo.setMapa(null);
			ofy.put(surdo);
		}
		return retorno;
	}
	
	public void apagarSurdo(Long id) {
		Objectify ofy = ObjectifyService.begin();
		ofy.delete(Surdo.class, id);
	}
	
	public void apagarMapa(Long identificadorMapa) {
		Objectify ofy = ObjectifyService.begin();
		ofy.delete(Mapa.class, identificadorMapa);
	}
	
	public List<Surdo> obterSurdosNaoVisitar() {
		List<Surdo> retorno = new ArrayList<Surdo>();
		Objectify ofy = ObjectifyService.begin();
		
		Query<Surdo> mudouSe = ofy.query(Surdo.class);		
		mudouSe.filter("mudouSe", Boolean.TRUE);
		retorno.addAll(mudouSe.list());
		
		Query<Surdo> visitarSomentePorAnciaos = ofy.query(Surdo.class);
		visitarSomentePorAnciaos.filter("visitarSomentePorAnciaos", Boolean.TRUE);
		retorno.addAll(visitarSomentePorAnciaos.list());
		
		return retorno;
	}
}
