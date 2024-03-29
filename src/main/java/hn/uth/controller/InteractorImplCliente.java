package hn.uth.controller;

import hn.uth.data.ClienteResponse;
import hn.uth.model.DatabaseRepositoryImpl;
import hn.uth.views.cliente.ViewModelCliente;

public class InteractorImplCliente implements InteractorCliente{
	private DatabaseRepositoryImpl modelo;
	private ViewModelCliente vista;
	
	public InteractorImplCliente(ViewModelCliente view) {
		super();
		this.vista = view;
		this.modelo = DatabaseRepositoryImpl.getInstance("https://apex.oracle.com", 30000L);
	}

	@Override
	public void consultarCliente() {
		try {
			ClienteResponse respuesta = this.modelo.consultarCliente();
			if(respuesta == null ||respuesta.getCount() == 0 || respuesta.getItems() == null) {
				this.vista.mostrarMensajeError("No hay Cliente que mostrar");
				
			}else {
				this.vista.mostrarClientesEnGrid(respuesta.getItems());
			}
			
		}catch (Exception error) {
			error.printStackTrace();
		}
	}
	
	

}
