package br.com.nascisoft.apoioterritoriols.cadastro.client.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import br.com.nascisoft.apoioterritoriols.cadastro.client.CadastroServiceAsync;
import br.com.nascisoft.apoioterritoriols.cadastro.client.event.EditarSurdoEvent;
import br.com.nascisoft.apoioterritoriols.cadastro.client.event.PesquisarSurdoEvent;
import br.com.nascisoft.apoioterritoriols.cadastro.client.view.CadastroSurdoView;
import br.com.nascisoft.apoioterritoriols.cadastro.vo.GeocoderResultVO;
import br.com.nascisoft.apoioterritoriols.cadastro.vo.SurdoDetailsVO;
import br.com.nascisoft.apoioterritoriols.login.entities.Bairro;
import br.com.nascisoft.apoioterritoriols.login.entities.Cidade;
import br.com.nascisoft.apoioterritoriols.login.entities.Regiao;
import br.com.nascisoft.apoioterritoriols.login.entities.Surdo;
import br.com.nascisoft.apoioterritoriols.login.util.StringUtils;
import br.com.nascisoft.apoioterritoriols.login.vo.LoginVO;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CadastroSurdoPresenter extends AbstractCadastroPresenter implements CadastroSurdoView.Presenter {

	private final CadastroSurdoView view;
	
	public CadastroSurdoPresenter(CadastroServiceAsync service,
			HandlerManager eventBus, CadastroSurdoView view, LoginVO login) {
		super(service, eventBus, login);
		this.view = view;
		this.view.setPresenter(this);
		this.populaCidades();
	}
	
	@Override
	public void initView() {
		super.initView();
	}
	
	
	@Override
	public void onManterCidadeListBoxChange(final Long cidadeId) {
		getView().showWaitingPanel();
		GWT.log("Entrando em onManterCidadeListBoxChange, id da cidade: " +cidadeId);
		service.obterRegioes(cidadeId, new AsyncCallback<List<Regiao>>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao obter lista de regiões.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao obter lista de regiões. \n" + caught.getMessage());					
			}

			@Override
			public void onSuccess(List<Regiao> result) {
				GWT.log("Sucesso ao obter regiões, regiões encontradas: " + result.size());
				getView().setManterRegiaoList(result);
				populaBairros(cidadeId);
				getView().hideWaitingPanel();
			}
		});
	}
	
	protected void populaBairros(Long cidadeId) {
		getView().showWaitingPanel();
		service.obterBairros(cidadeId, new AsyncCallback<List<Bairro>>() {
			
			@Override
			public void onSuccess(List<Bairro> result) {
				List<String> bairros = new ArrayList<String>();
				for (Bairro bairro : result) {
					bairros.add(bairro.getNome());
				}
				getView().setBairroList(bairros);
				getView().hideWaitingPanel();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao obter lista de bairros.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao obter lista de bairros. \n" + caught.getMessage());					
			}
		});
	}

	@Override
	public void onPesquisaPesquisarButtonClick(String identificadorCidade, String nomeSurdo, String nomeRegiao, String identificadorMapa, Boolean estaAssociadoMapa) {
		eventBus.fireEvent(new PesquisarSurdoEvent(identificadorCidade, nomeSurdo, nomeRegiao, identificadorMapa, estaAssociadoMapa));
	}
	
	@Override
	public void onPesquisaPesquisarEvent(final String identificadorCidade, final String nomeSurdo, final String regiaoId, final String identificadorMapa, final Boolean estaAssociadoMapa) {
		getView().showWaitingPanel();
		Long mapa = null;
		Long cidade = null;
		if (!StringUtils.isEmpty(identificadorCidade)) {
			cidade = Long.valueOf(identificadorCidade);
		}
		if (!StringUtils.isEmpty(identificadorMapa)) {
			mapa = Long.valueOf(identificadorMapa);
		}
		Long regiao = null;
		if (!StringUtils.isEmpty(regiaoId)) {
			regiao = Long.valueOf(regiaoId);
		}
		service.obterSurdos(cidade, nomeSurdo, regiao, mapa, estaAssociadoMapa, new AsyncCallback<List<SurdoDetailsVO>>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao obter lista de surdos.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao obter lista de surdos. \n" + caught.getMessage());		
			}

			@Override
			public void onSuccess(List<SurdoDetailsVO> result) {
				view.setResultadoPesquisa(result);
				getView().hideWaitingPanel();
			}
		});			
	}

	@Override
	public void adicionarOuAlterarSurdo(Surdo surdo) {
		getView().showWaitingPanel();
		service.adicionarOuAlterarSurdo(surdo, new AsyncCallback<Long>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao adicionar ou alterar surdo.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao adicionar ou alterar surdo. \n" + caught.getMessage());
			}

			@Override
			public void onSuccess(Long result) {
				getView().hideWaitingPanel();
				Window.alert("Surdo id " + result + " salvo com sucesso.");
				eventBus.fireEvent(new PesquisarSurdoEvent("", "", "", "", null));
			}
		});
	}

	@Override
	public void onEditarButtonClick(Long id) {
		eventBus.fireEvent(new EditarSurdoEvent(id));
	}

	@Override
	public void onEditar(Long id) {
		getView().showWaitingPanel();
		service.obterSurdo(id, new AsyncCallback<Surdo>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao obter surdo.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao obter surdo. \n" + caught.getMessage());
			}
			
			@Override
			public void onSuccess(Surdo result) {
				view.onEditar(result);			
				getView().hideWaitingPanel();
			}
		});
	}

	@Override
	public void onVisualizar(Long id) {
		getView().showWaitingPanel();
		service.obterSurdo(id, new AsyncCallback<Surdo>() {
			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao obter surdo.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao obter surdo. \n" + caught.getMessage());
			}

			@Override
			public void onSuccess(Surdo result) {
				view.onVisualizar(result);			
				getView().hideWaitingPanel();
			}
		});
	}

	@Override
	public void setTabSelectionEventHandler(SelectionHandler<Integer> handler) {
		this.view.setTabSelectionEventHandler(handler);
	}

	@Override
	CadastroSurdoView getView() {
		return this.view;
	}	

	@Override
	public void onApagar(Long id) {
		getView().showWaitingPanel();
		this.service.apagarSurdo(id, new AsyncCallback<Long>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao apagar surdo.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao apagar surdo. \n" + caught.getMessage());
			}

			@Override
			public void onSuccess(Long result) {
				view.onApagarSurdo(result);
				getView().hideWaitingPanel();
			}
		});
	}

	@Override
	public void buscarEndereco(final Long identificadorCidade, final String logradouro, final String numero, final String bairro,
			final String cep) {
		getView().showWaitingPanel();
		
		
		service.obterCidade(identificadorCidade, new AsyncCallback<Cidade>() {
			
			@Override
			public void onSuccess(final Cidade cidade) {
				
				StringBuilder sb = new StringBuilder();
				String separador = ", ";
				sb.append(logradouro).append(separador).
				   append(numero).append(separador);
			
				if (!StringUtils.isEmpty(bairro) && cidade.getUtilizarBairroBuscaEndereco()) {
					sb.append(bairro).append(separador);
				}
				
				if (!StringUtils.isEmpty(cep)) {
					sb.append(cep).append(separador);
				}
				
				sb.append(cidade.getNome()).append(separador).append(cidade.getUF()).append(separador).append(cidade.getPais());
				
				service.buscarEndereco(sb.toString(), new AsyncCallback<GeocoderResultVO>() {
					@Override
					public void onFailure(Throwable caught) {
						logger.log(Level.SEVERE, "Falha ao buscar endereço.\n", caught);
						getView().hideWaitingPanel();
						Window.alert("Falha ao buscar endereço. \n" + caught.getMessage());
					}
					
					public void onSuccess(GeocoderResultVO result) {
						HasLatLng centro = roundLatLng(new LatLng(cidade.getLatitudeCentro(), cidade.getLongitudeCentro()));
						if ("OK".equals(result.getStatus())) {
							HasLatLng retorno = roundLatLng(new LatLng(result.getLat(), result.getLng()));
							boolean naoEncontrouEndereco = retorno.equals(centro);
							if (naoEncontrouEndereco) {
								retorno = new LatLng(cidade.getLatitudeCentroTerritorio(), cidade.getLongitudeCentroTerritorio());
							} else {
								retorno = new LatLng(result.getLat(), result.getLng());
							}
							view.setPosition(retorno, !naoEncontrouEndereco, true);
						} else {
							view.setPosition(centro, false, true);
						}				
						getView().hideWaitingPanel();
						
					};
				});
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Falha ao buscar cidade.\n", caught);
				getView().hideWaitingPanel();
				Window.alert("Falha ao buscar cidade. \n" + caught.getMessage());
			}
		});
		
	}
	
	private HasLatLng roundLatLng(HasLatLng latlng) {
		return new LatLng(Math.floor(latlng.getLatitude()*1000)/1000, Math.floor(latlng.getLongitude()*1000)/1000);
	}

	@Override
	public void onAdicionar() {
		getView().onAdicionar();
	}

}
